package dev.pablodiste.datastore.sample.repositories.store.room.dao;

import androidx.room.Dao
import dev.pablodiste.datastore.adapters.room.RoomListSourceOfTruth
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.database.RoomDatabase
import dev.pablodiste.datastore.sample.models.room.Post

@Dao
abstract class PostsSourceOfTruth: RoomListSourceOfTruth<NoKey, Post>("posts", RoomDatabase.roomDb) {
    override fun query(key: NoKey): String = ""
}
