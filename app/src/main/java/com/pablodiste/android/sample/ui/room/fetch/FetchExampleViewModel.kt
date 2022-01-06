package com.pablodiste.android.sample.ui.room.fetch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.repositories.store.room.RoomPersonStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FetchExampleViewModel : ViewModel() {

    private val personStore = RoomPersonStore()
    val uiState = MutableStateFlow(People())

    init {
        viewModelScope.launch {
            val people = personStore.fetch(RoomPersonStore.Key("1")).requireData()
            Log.d(TAG, "Fetch response: $people")
            uiState.value = people
        }
    }

    companion object {
        private val TAG: String = FetchExampleViewModel::class.java.simpleName
    }
}