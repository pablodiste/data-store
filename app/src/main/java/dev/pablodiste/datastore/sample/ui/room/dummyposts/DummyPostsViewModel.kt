package dev.pablodiste.datastore.sample.ui.room.dummyposts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pablodiste.datastore.impl.stream
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.repositories.store.room.provideDummyPostsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DummyPostsViewModel : ViewModel() {

    private val postsStore = provideDummyPostsStore()
    val uiState = MutableStateFlow<List<DummyPost>>(listOf())

    init {
        viewModelScope.launch {
            postsStore.stream(refresh = true).collect { result ->
                Log.d(TAG, "Received $result")
                uiState.value = result.requireData()
            }
        }
    }

    companion object {
        private val TAG: String = DummyPostsViewModel::class.java.simpleName
    }
}