package dev.pablodiste.datastore.sample.ui.room.dummyposts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pablodiste.datastore.get
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.repositories.store.room.DummyPostId
import dev.pablodiste.datastore.sample.repositories.store.room.provideDummyPostStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DummyPostViewModel(val postId: Int) : ViewModel() {

    private val postStore = provideDummyPostStore()
    val uiState = MutableStateFlow<DummyPost?>(null)

    init {
        viewModelScope.launch {
            val response = postStore.get(DummyPostId(postId)).requireData()
            Log.d(TAG, "Response: $response")
            uiState.value = response
        }
    }

    companion object {
        private val TAG: String = DummyPostViewModel::class.java.simpleName
    }

    suspend fun delete() {
        val post = uiState.value!!
        postStore.delete(DummyPostId(post.id), post)
    }

}