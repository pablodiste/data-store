package com.pablodiste.android.sample.ui.room.errors

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.StoreConfig
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.repositories.store.room.RoomPersonStore
import com.pablodiste.android.sample.repositories.store.room.RoomPersonStoreWithError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ErrorExampleViewModel : ViewModel() {

    private val personStore = RoomPersonStoreWithError()
    val uiState = MutableStateFlow(State())
    val throttlingState = StoreConfig.throttlingController.throttlingState

    class State(
        var loadingError: Boolean = false,
        var data: People = People()
    )

    init {
        makeRequest()
    }

    fun makeRequest() {
        /*
        // One alternative for error handling
        viewModelScope.launch {
            val result = personStore.fetch(RoomPersonStore.Key("1"))
            when (result) {
                is StoreResponse.Data -> uiState.value.data = result.value
                is StoreResponse.Error -> uiState.value.loadingError = true
                else -> {}
            }
        }
        */

        // Another alternative for error handling in Flows
        viewModelScope.launch {
            personStore.stream(RoomPersonStore.Key("1"), refresh = true)
                .map {
                    Log.d(TAG, "Received $it")
                    it.requireData()
                }
                .catch { uiState.value.loadingError = true }
                .collect { result -> uiState.value.data = result }
        }
    }

    companion object {
        private val TAG: String = ErrorExampleViewModel::class.java.simpleName
    }
}