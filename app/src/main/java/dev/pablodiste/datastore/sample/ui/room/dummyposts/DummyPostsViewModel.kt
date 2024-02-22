package dev.pablodiste.datastore.sample.ui.room.dummyposts

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pablodiste.datastore.StoreRequest
import dev.pablodiste.datastore.StoreResponse
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.sample.models.room.DummyPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DummyPostsViewModel @Inject constructor(
    private val postsStore: SimpleStoreImpl<NoKey, List<DummyPost>>
) : ViewModel() {

    val uiState = MutableStateFlow<List<DummyPost>>(listOf())
    val loading = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            postsStore.stream(StoreRequest(key = NoKey(), refresh = true, emitLoadingStates = true)).collect { result ->
                Log.d(TAG, "Received $result")
                when (result) {
                    is StoreResponse.Data -> {
                        Log.d(TAG, "Received Data")
                        uiState.value = result.requireData()
                        loading.value = false
                    }
                    is StoreResponse.Loading -> {
                        Log.d(TAG, "Received Loading")
                        loading.value = true
                    }
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