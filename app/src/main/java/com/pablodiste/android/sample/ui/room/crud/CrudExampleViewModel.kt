package com.pablodiste.android.sample.ui.room.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablodiste.android.datastore.StoreResponse
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
            val response = postsStore.create(PostKey(id = 1), Post(title = "Title", body = "Body", userId = 1))
            when (response) {
                is StoreResponse.Data -> uiState.value = "Created"
                is StoreResponse.Error -> uiState.value = "Error in create"
                else -> {}
            }
        }
    }

    fun update() {
        viewModelScope.launch {
            val response = postsStore.update(PostKey(id = 1), Post(title = "Title", body = "Body", userId = 1))
            when (response) {
                is StoreResponse.Data -> uiState.value = "Updated"
                is StoreResponse.Error -> uiState.value = "Error in update"
                else -> {}
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            val response = postsStore.delete(PostKey(id = 1), Post(title = "Title", body = "Body", userId = 1))
            when (response) {
                true -> uiState.value = "Deleted"
                false -> uiState.value = "Error in delete"
            }
        }
    }

}