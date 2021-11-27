package com.pablodiste.android.sample.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.sample.repositories.store.PeopleStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val peopleStore = PeopleStore()

    fun getPeople() = viewModelScope.launch {
        peopleStore.stream(refresh = true).map { it.value }.collect { people ->
            people.forEach { Log.d(TAG, "People: $it") }
        }
    }

    companion object {
        private val TAG: String = MainViewModel::class.java.simpleName
    }
}