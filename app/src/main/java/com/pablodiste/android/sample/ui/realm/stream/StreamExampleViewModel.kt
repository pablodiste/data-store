package com.pablodiste.android.sample.ui.realm.stream

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.StoreResponse
import com.pablodiste.android.datastore.closable.autoClose
import com.pablodiste.android.datastore.closable.stream
import com.pablodiste.android.sample.models.realm.People
import com.pablodiste.android.sample.repositories.store.realm.RealmPeopleStore
import com.pablodiste.android.sample.repositories.store.realm.RealmPlanetsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

class StreamExampleViewModel : ViewModel() {

    private val peopleStore1 = RealmPeopleStore()
    private val peopleStore2 = RealmPeopleStore()
    private val planetStore = RealmPlanetsStore()
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