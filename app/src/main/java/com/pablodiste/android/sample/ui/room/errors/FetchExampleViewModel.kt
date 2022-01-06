package com.pablodiste.android.sample.ui.room.errors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.StoreResponse
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.repositories.store.room.RoomPersonStore
import com.pablodiste.android.sample.repositories.store.room.RoomPersonStoreWithError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ErrorExampleViewModel : ViewModel() {

    private val personStore = RoomPersonStoreWithError()
    val uiState = MutableStateFlow(State())

    class State(
        var loadingError: Boolean = false,
        var data: People = People()
    )

    init {
        viewModelScope.launch {
            val result = personStore.fetch(RoomPersonStore.Key("1"))
            when (result) {
                is StoreResponse.Data -> uiState.value.data = result.value
                is StoreResponse.Error -> uiState.value.loadingError = true
                else -> {}
            }
        }
    }

    companion object {
        private val TAG: String = ErrorExampleViewModel::class.java.simpleName
    }
}