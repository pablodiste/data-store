package com.pablodiste.android.sample.ui.fetch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.sample.models.realm.People
import com.pablodiste.android.sample.repositories.store.PersonStore
import com.pablodiste.android.sample.ui.stream1.StreamExampleViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FetchExampleViewModel : ViewModel() {

    private val personStore = PersonStore(viewModelScope)
    val uiState = MutableStateFlow<People>(People())

    init {
        viewModelScope.launch {
            val response = personStore.fetch(PersonStore.Key("1"))
            Log.d(TAG, "Fetch response: ${response.value}")
            uiState.value = response.value
        }
    }

    companion object {
        private val TAG: String = StreamExampleViewModel::class.java.simpleName
    }
}