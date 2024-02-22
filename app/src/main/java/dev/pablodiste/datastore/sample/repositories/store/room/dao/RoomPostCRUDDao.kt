package dev.pablodiste.datastore.sample.repositories.store.room.dao;

import androidx.room.Dao
import dev.pablodiste.datastore.adapters.room.RoomSourceOfTruth
import dev.pablodiste.datastore.sample.models.room.Post

data class PostKey(val id: Int)

@Dao
abstract class PostSourceOfTruth: RoomSourceOfTruth<PostKey, Post>("posts", dev.pablodiste.datastore.sample.SampleApplication.roomDb) {
    override fun query(key: PostKey): String = "id = ${key.id}"
}
