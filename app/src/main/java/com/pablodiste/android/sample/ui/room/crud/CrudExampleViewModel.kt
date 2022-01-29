package com.pablodiste.android.sample.ui.room.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.sample.models.room.Post
import com.pablodiste.android.sample.repositories.store.room.PostKey
import com.pablodiste.android.sample.repositories.store.room.providePostsCRUDStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RoomCrudExampleViewModel : ViewModel() {

    private val postsStore = providePostsCRUDStore()
    val uiState = MutableStateFlow("")

    companion object {
        private val TAG: String = RoomCrudExampleViewModel::class.java.simpleName
    }

    fun create() {
        viewModelScope.launch {
            postsStore.create(PostKey(id = 1), Post(title = "Title", body = "Body", userId = 1))
            uiState.value = "Created"
        }
    }

    fun update() {
        viewModelScope.launch {
            postsStore.update(PostKey(id = 1), Post(title = "Title", body = "Body", userId = 1))
            uiState.value = "Updated"
        }
    }

    fun delete() {
        viewModelScope.launch {
            postsStore.delete(PostKey(id = 1), Post(title = "Title", body = "Body", userId = 1))
            uiState.value = "Deleted"
        }
    }

}