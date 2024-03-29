package dev.pablodiste.datastore.sample.ui.realm.stream

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pablodiste.datastore.closable.autoClose
import dev.pablodiste.datastore.closable.stream
import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.repositories.store.realm.RealmPeopleStore
import dev.pablodiste.datastore.sample.repositories.store.realm.RealmPlanetsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreamExampleViewModel @Inject constructor(
    private val peopleStore1: RealmPeopleStore,
    private val peopleStore2: RealmPeopleStore,
    private val planetStore: RealmPlanetsStore) : ViewModel() {

    val uiState = MutableStateFlow<List<People>>(listOf())

    init {
        viewModelScope.launch {
            peopleStore1.stream(refresh = true).map { it.requireData() }.collect { result ->
                Log.d(TAG, "Stream 1: $result Received new People")
                uiState.value = result
            }
        }.autoClose(peopleStore1)
        /*
        viewModelScope.launch(peopleStore2) {
            peopleStore2.stream(refresh = true).collect { result ->
                Log.d(TAG, "Stream 2: ${result.origin} Received new People")
                //people.forEach { Log.d(TAG, "People: $it") }
                uiState.value = result.value
            }
        }
        viewModelScope.launch(planetStore) {
            planetStore.stream(refresh = true).collect { result ->
                Log.d(TAG, "Stream 3: ${result.origin} Received new Planets")
            }
        }
         */
        //uiState.value = peopleStore.get().value
        peopleStore1.something()

        viewModelScope.coroutineContext.job.invokeOnCompletion {
            Log.d(TAG, "onCompletion")
        }

    }

    companion object {
        private val TAG: String = StreamExampleViewModel::class.java.simpleName
    }
}