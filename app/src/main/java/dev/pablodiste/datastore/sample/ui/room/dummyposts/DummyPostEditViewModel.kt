package dev.pablodiste.datastore.sample.ui.room.dummyposts

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pablodiste.datastore.crud.SimpleCrudStoreImpl
import dev.pablodiste.datastore.get
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.repositories.store.room.dao.DummyPostId
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DummyPostEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postStore: SimpleCrudStoreImpl<DummyPostId, DummyPost>) : ViewModel() {

    var uiState by mutableStateOf<DummyPost?>(null)

    init {
        val postId = savedStateHandle.get<Int>("postId") ?: 0
        viewModelScope.launch {
            val response = postStore.get(DummyPostId(postId)).requireData()
            Log.d(TAG, "Response: $response")
            uiState = response
        }
    }

    companion object {
        private val TAG: String = DummyPostEditViewModel::class.java.simpleName
    }

    suspend fun update() {
        postStore.update(DummyPostId(uiState!!.id), uiState!!)
    }

}