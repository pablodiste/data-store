package com.pablodiste.android.sample.ui.room.stream

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.impl.stream
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.repositories.store.room.RoomPeopleStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RoomStreamExampleViewModel : ViewModel() {

    private val peopleStore1 = RoomPeopleStore()
    val uiState = MutableStateFlow<List<People>>(listOf())

    init {
        viewModelScope.launch {
            peopleStore1.stream(refresh = true).map { it.requireData() }.collect { result ->
                Log.d(TAG, "Stream 1: $result Received new People")
                uiState.value = result
            }
        }
    }

    companion object {
        private val TAG: String = RoomStreamExampleViewModel::class.java.simpleName
    }
}