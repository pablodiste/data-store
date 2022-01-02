package com.pablodiste.android.sample.ui.realm.get

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.closable.launch
import com.pablodiste.android.sample.models.realm.People
import com.pablodiste.android.sample.repositories.store.realm.RealmPersonStore
import kotlinx.coroutines.flow.MutableStateFlow

class GetExampleViewModel : ViewModel() {

    private val personStore = RealmPersonStore()
    val uiState = MutableStateFlow<People>(People())

    init {
        viewModelScope.launch(personStore) {
            val response = personStore.get(RealmPersonStore.Key("1"))
            Log.d(TAG, "Fetch response: ${response.value}")
            uiState.value = response.value
        }
    }

    companion object {
        private val TAG: String = GetExampleViewModel::class.java.simpleName
    }
}