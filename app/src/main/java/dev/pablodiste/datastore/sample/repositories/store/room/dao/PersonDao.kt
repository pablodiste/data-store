package dev.pablodiste.datastore.sample.repositories.store.room.dao

import androidx.room.Dao
import dev.pablodiste.datastore.adapters.room.RoomSourceOfTruth
import dev.pablodiste.datastore.sample.database.RoomDatabase
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPersonStore

@Dao
abstract class PersonSourceOfTruth: RoomSourceOfTruth<RoomPersonStore.Key, People>("people", RoomDatabase.roomDb) {
    override fun query(key: RoomPersonStore.Key): String = "id = ${key.id}"
}
