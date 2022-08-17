package dev.pablodiste.datastore.sample.ui.realm.get

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pablodiste.datastore.StoreResponse
import dev.pablodiste.datastore.closable.launch
import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.repositories.store.realm.RealmPersonStore
import kotlinx.coroutines.flow.MutableStateFlow

class GetExampleViewModel : ViewModel() {

    private val personStore = RealmPersonStore()
    val uiState = MutableStateFlow<People>(People())

    init {
        viewModelScope.launch(personStore) {
            val person = personStore.get(RealmPersonStore.Key("1")).requireData()
            Log.d(TAG, "Fetch response: $person")
            uiState.value = person
        }
    }

    companion object {
        private val TAG: String = GetExampleViewModel::class.java.simpleName
    }
}