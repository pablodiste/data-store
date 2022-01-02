package com.pablodiste.android.sample.ui.room.stream

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.impl.stream
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.repositories.store.RoomPeopleStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RoomStreamExampleViewModel : ViewModel() {

    private val peopleStore1 = RoomPeopleStore()
    val uiState = MutableStateFlow<List<People>>(listOf())

    init {
        viewModelScope.launch {
            peopleStore1.stream(refresh = true).collect { result ->
                Log.d(TAG, "Stream 1: ${result.origin} Received new People")
                uiState.value = result.value
            }
        }
    }

    companion object {
        private val TAG: String = RoomStreamExampleViewModel::class.java.simpleName
    }
}