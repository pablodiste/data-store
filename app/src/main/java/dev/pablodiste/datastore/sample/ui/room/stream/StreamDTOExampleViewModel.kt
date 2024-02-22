package dev.pablodiste.datastore.sample.ui.room.stream

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pablodiste.datastore.Store
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.models.room.Starship
import dev.pablodiste.datastore.stream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomStreamDTOExampleViewModel @Inject constructor(private val starshipStore: Store<NoKey, List<Starship>>) : ViewModel() {

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