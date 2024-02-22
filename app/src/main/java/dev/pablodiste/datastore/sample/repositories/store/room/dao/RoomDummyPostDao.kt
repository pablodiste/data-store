package dev.pablodiste.datastore.sample.repositories.store.room.dao;

import androidx.room.Dao
import dev.pablodiste.datastore.adapters.room.RoomListSourceOfTruth
import dev.pablodiste.datastore.adapters.room.RoomSourceOfTruth
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.models.room.DummyPost

data class DummyPostId(val id: Int)

@Dao
abstract class DummyPostsSourceOfTruth: RoomListSourceOfTruth<NoKey, DummyPost>("dummy_posts", SampleApplication.roomDb) {
    override fun query(key: NoKey): String = ""
}

@Dao
abstract class DummyPostSourceOfTruth: RoomSourceOfTruth<DummyPostId, DummyPost>("dummy_posts", SampleApplication.roomDb) {
    override fun query(key: DummyPostId): String = "id = ${key.id}"
}
