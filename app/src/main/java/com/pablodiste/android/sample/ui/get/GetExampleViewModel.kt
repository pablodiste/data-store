package com.pablodiste.android.sample.ui.get

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.closable.launch
import com.pablodiste.android.sample.models.realm.People
import com.pablodiste.android.sample.repositories.store.PersonStore
import com.pablodiste.android.sample.ui.stream1.StreamExampleViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GetExampleViewModel : ViewModel() {

    private val personStore = PersonStore()
    val uiState = MutableStateFlow<People>(People())

    init {
        viewModelScope.launch(personStore) {
            val response = personStore.get(PersonStore.Key("1"))
            Log.d(TAG, "Fetch response: ${response.value}")
            uiState.value = response.value
        }
    }

    companion object {
        private val TAG: String = GetExampleViewModel::class.java.simpleName
    }
}