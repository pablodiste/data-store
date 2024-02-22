package dev.pablodiste.datastore.sample.ui.room.get

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pablodiste.datastore.get
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPersonStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GetExampleViewModel @Inject constructor(private val personStore: RoomPersonStore) : ViewModel() {

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