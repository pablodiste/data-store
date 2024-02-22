package dev.pablodiste.datastore.sample.ui.room.fetch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pablodiste.datastore.Store
import dev.pablodiste.datastore.fetch
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPersonStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FetchExampleViewModel @Inject constructor(private val personStore: Store<RoomPersonStore.Key, People>) : ViewModel() {

    val uiState = MutableStateFlow(People())

    init {
        viewModelScope.launch {
            val result = personStore.fetch(RoomPersonStore.Key("1"))
            // Or you can avoid the rate limiter with the force parameter
            // val result = personStore.fetch(RoomPersonStore.Key("1"), forced = true)
            Log.d(TAG, "Received $result")
            uiState.value = result.requireData()
        }
    }

    companion object {
        private val TAG: String = FetchExampleViewModel::class.java.simpleName
    }
}