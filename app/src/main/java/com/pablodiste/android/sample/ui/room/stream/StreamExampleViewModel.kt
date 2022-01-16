package com.pablodiste.android.sample.ui.room.stream

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.StoreResponse
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