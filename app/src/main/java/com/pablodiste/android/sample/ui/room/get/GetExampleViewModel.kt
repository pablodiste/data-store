package com.pablodiste.android.sample.ui.room.get

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.repositories.store.room.RoomPersonStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GetExampleViewModel : ViewModel() {

    private val personStore = RoomPersonStore()
    val uiState = MutableStateFlow(People())

    init {
        viewModelScope.launch {
            val response = personStore.get(RoomPersonStore.Key("1"))
            Log.d(TAG, "Fetch response: ${response.value}")
            uiState.value = response.value
        }
    }

    companion object {
        private val TAG: String = GetExampleViewModel::class.java.simpleName
    }
}