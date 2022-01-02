package com.pablodiste.android.sample.ui.realm.fetch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.closable.launch
import com.pablodiste.android.sample.models.realm.People
import com.pablodiste.android.sample.repositories.store.realm.RealmPersonStore
import kotlinx.coroutines.flow.MutableStateFlow

class FetchExampleViewModel : ViewModel() {

    private val personStore = RealmPersonStore()
    val uiState = MutableStateFlow<People>(People())

    init {
        viewModelScope.launch(personStore) {
            val response = personStore.fetch(RealmPersonStore.Key("1"))
            Log.d(TAG, "Fetch response: ${response.value}")
            uiState.value = response.value
        }
    }

    companion object {
        private val TAG: String = FetchExampleViewModel::class.java.simpleName
    }
}