package com.pablodiste.android.sample.ui.room.concurrent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.impl.stream
import com.pablodiste.android.sample.models.room.Post
import com.pablodiste.android.sample.repositories.store.room.providePostsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RoomConcurrentExampleViewModel : ViewModel() {

    private val postsStore = providePostsStore()
    val uiState = MutableStateFlow<List<Post>>(listOf())

    init {
        // Here we are launching two concurrent service calls.
        // The rate limiter will make only one to emit the fetcher response.
        viewModelScope.launch {
            postsStore.stream(refresh = true).collect { result ->
                Log.d(TAG, "Received (1) [${result.requireOrigin()}] ${result.requireData().size} items")
                uiState.value = result.requireData()
            }
        }
        viewModelScope.launch {
            postsStore.stream(refresh = true).collect { result ->
                Log.d(TAG, "Received (2) [${result.requireOrigin()}] ${result.requireData().size} items")
                uiState.value = result.requireData()
            }
        }
    }

    companion object {
        private val TAG: String = RoomConcurrentExampleViewModel::class.java.simpleName
    }
}