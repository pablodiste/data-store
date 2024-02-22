package dev.pablodiste.datastore.sample.ui.realm.fetch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pablodiste.datastore.closable.launch
import dev.pablodiste.datastore.fetch
import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.repositories.store.realm.RealmPersonStore
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class FetchExampleViewModel @Inject constructor(private val personStore: RealmPersonStore) : ViewModel() {

    val uiState = MutableStateFlow(People())

    init {
        viewModelScope.launch(personStore) {
            val people = personStore.fetch(RealmPersonStore.Key("1")).requireData()
            Log.d(TAG, "Fetch response: $people")
            uiState.value = people
        }
    }

    companion object {
        private val TAG: String = FetchExampleViewModel::class.java.simpleName
    }
}