package dev.pablodiste.datastore.sample.repositories.store.room.dao

import androidx.room.Dao
import dev.pablodiste.datastore.adapters.room.RoomListSourceOfTruth
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.models.room.Starship

@Dao
abstract class StarshipSourceOfTruth: RoomListSourceOfTruth<NoKey, Starship>("starships", SampleApplication.roomDb) {
    override fun query(key: NoKey): String = ""
}
