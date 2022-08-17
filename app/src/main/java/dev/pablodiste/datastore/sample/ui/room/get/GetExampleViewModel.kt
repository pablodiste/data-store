package dev.pablodiste.datastore.sample.ui.room.get

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPersonStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GetExampleViewModel : ViewModel() {

    private val personStore = RoomPersonStore()
    val uiState = MutableStateFlow(People())

    init {
        viewModelScope.launch {
            val result = personStore.get(RoomPersonStore.Key("1"))
            Log.d(TAG, "Received $result")
            uiState.value = result.requireData()
        }
    }

    companion object {
        private val TAG: String = GetExampleViewModel::class.java.simpleName
    }
}