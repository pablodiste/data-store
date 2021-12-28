package com.pablodiste.android.sample.ui.stream1

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.closable.launch
import com.pablodiste.android.datastore.closable.stream
import com.pablodiste.android.sample.models.realm.People
import com.pablodiste.android.sample.repositories.store.PeopleStore
import com.pablodiste.android.sample.repositories.store.PlanetsStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.job

class StreamExampleViewModel : ViewModel() {

    private val peopleStore1 = PeopleStore(viewModelScope)
    private val peopleStore2 = PeopleStore(viewModelScope)
    private val planetStore = PlanetsStore(viewModelScope)
    val uiState = MutableStateFlow<List<People>>(listOf())

    init {
        viewModelScope.launch(peopleStore1) {
            peopleStore1.stream(refresh = true).collect { result ->
                Log.d(TAG, "Stream 1: ${result.origin} Received new People")
                //people.forEach { Log.d(TAG, "People: $it") }
                uiState.value = result.value
            }
        }
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