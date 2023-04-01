package dev.pablodiste.datastore.sample.ui.room.dummyposts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pablodiste.datastore.StoreRequest
import dev.pablodiste.datastore.StoreResponse
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.impl.fetch
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.repositories.store.room.provideDummyPostsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DummyPostsViewModel : ViewModel() {

    private val postsStore = provideDummyPostsStore()
    val uiState = MutableStateFlow<List<DummyPost>>(listOf())
    val loading = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            postsStore.stream(StoreRequest(key = NoKey(), refresh = true, emitLoadingStates = true)).collect { result ->
                Log.d(TAG, "Received $result")
                when (result) {
                    is StoreResponse.Data -> {
                        uiState.value = result.requireData()
                        loading.value = false
                    }
                    is StoreResponse.Loading -> loading.value = true
                    else -> { Log.d(TAG, "Error or NoData") }
                }
            }
        }
    }

    suspend fun refresh() {
        postsStore.fetch(StoreRequest(key = NoKey(), emitLoadingStates = true))
    }

    companion object {
        private val TAG: String = DummyPostsViewModel::class.java.simpleName
    }
}