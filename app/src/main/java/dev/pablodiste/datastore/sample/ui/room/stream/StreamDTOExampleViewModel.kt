package dev.pablodiste.datastore.sample.ui.room.stream

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.models.room.Starship
import dev.pablodiste.datastore.sample.repositories.store.room.provideStarshipStore
import dev.pablodiste.datastore.sample.repositories.store.room.provideStarshipStoreKtor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RoomStreamDTOExampleViewModel : ViewModel() {

    private val starshipStore = provideStarshipStoreKtor()
    val uiState = MutableStateFlow<List<Starship>>(listOf())

    init {
        viewModelScope.launch {
            starshipStore.stream(NoKey(), refresh = true).collect { result ->
                Log.d(TAG, "Received $result")
                uiState.value = result.requireData()
            }
        }
    }

    companion object {
        private val TAG: String = RoomStreamDTOExampleViewModel::class.java.simpleName
    }
}