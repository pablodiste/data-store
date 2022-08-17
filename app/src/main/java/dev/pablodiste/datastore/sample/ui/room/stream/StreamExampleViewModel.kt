package dev.pablodiste.datastore.sample.ui.room.stream

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pablodiste.datastore.StoreResponse
import dev.pablodiste.datastore.impl.stream
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPeopleStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RoomStreamExampleViewModel : ViewModel() {

    private val peopleStore1 = RoomPeopleStore()
    val uiState = MutableStateFlow<List<People>>(listOf())

    init {
        viewModelScope.launch {
            peopleStore1.stream(refresh = true).collect { result ->
                Log.d(TAG, "Received $result")
                uiState.value = result.requireData()
            }
        }
    }

    companion object {
        private val TAG: String = RoomStreamExampleViewModel::class.java.simpleName
    }
}