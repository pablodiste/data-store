package dev.pablodiste.datastore.sample.ui.room.stream

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pablodiste.datastore.impl.stream
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPeopleStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomStreamExampleViewModel @Inject constructor(private val roomPeopleStore: RoomPeopleStore) : ViewModel() {

    val uiState = MutableStateFlow<List<People>>(listOf())

    init {
        viewModelScope.launch {
            roomPeopleStore.stream(refresh = true).collect { result ->
                Log.d(TAG, "Received $result")
                uiState.value = result.requireData()
            }
        }
    }

    companion object {
        private val TAG: String = RoomStreamExampleViewModel::class.java.simpleName
    }
}