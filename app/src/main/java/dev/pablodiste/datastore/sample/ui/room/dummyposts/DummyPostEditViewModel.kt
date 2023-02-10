package dev.pablodiste.datastore.sample.ui.room.dummyposts

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pablodiste.datastore.get
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.repositories.store.room.DummyPostId
import dev.pablodiste.datastore.sample.repositories.store.room.provideDummyPostStore
import kotlinx.coroutines.launch

class DummyPostEditViewModel(private val postId: Int) : ViewModel() {

    private val postStore = provideDummyPostStore()
    var uiState by mutableStateOf<DummyPost?>(null)

    init {
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