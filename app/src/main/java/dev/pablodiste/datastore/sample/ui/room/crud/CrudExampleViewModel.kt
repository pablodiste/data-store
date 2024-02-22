package dev.pablodiste.datastore.sample.ui.room.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pablodiste.datastore.StoreResponse
import dev.pablodiste.datastore.crud.SimpleCrudStoreImpl
import dev.pablodiste.datastore.sample.models.room.Post
import dev.pablodiste.datastore.sample.repositories.store.room.dao.PostKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomCrudExampleViewModel @Inject constructor(private val postsStore: SimpleCrudStoreImpl<PostKey, Post>) : ViewModel() {

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