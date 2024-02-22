package dev.pablodiste.datastore.sample.ui.room.concurrent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.impl.stream
import dev.pablodiste.datastore.sample.models.room.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomConcurrentExampleViewModel @Inject constructor(private val postsStore: SimpleStoreImpl<NoKey, List<Post>>) : ViewModel() {

    val uiState = MutableStateFlow<List<Post>>(listOf())

    init {
        // Here we are launching two concurrent service calls.
        // The rate limiter will make only one to emit the fetcher response.
        viewModelScope.launch {
            postsStore.stream(refresh = true).collect { result ->
                Log.d(TAG, "Received (1) [${result.requireOrigin()}] ${result.requireData().size} items")
                uiState.value = result.requireData()
            }
        }.invokeOnCompletion {
            Log.d(TAG, "ViewModelScope Completed (1)")
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