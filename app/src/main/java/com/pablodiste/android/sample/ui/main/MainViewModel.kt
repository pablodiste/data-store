package com.pablodiste.android.sample.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pablodiste.android.sample.models.realm.People
import com.pablodiste.android.sample.repositories.store.PeopleStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val peopleStore = PeopleStore()
    val uiState = MutableStateFlow<List<People>>(listOf())

    fun getPeople() = viewModelScope.launch {

        /*
        peopleStore.stream(refresh = false).map { it.value }.collect { people ->
            Log.d(TAG, "Received new People")
            people.forEach { Log.d(TAG, "People: $it") }
            uiState.value = people
        }
        */
        uiState.value = peopleStore.get().value

        viewModelScope.coroutineContext[Job]?.invokeOnCompletion {
            Log.d(TAG, "onCompletion")
        }

    }

    companion object {
        private val TAG: String = MainViewModel::class.java.simpleName
    }
}