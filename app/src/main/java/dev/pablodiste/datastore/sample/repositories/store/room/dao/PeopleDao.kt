package dev.pablodiste.datastore.sample.repositories.store.room.dao

import androidx.room.Dao
import dev.pablodiste.datastore.adapters.room.DeleteAllNotInFetchStalenessPolicy
import dev.pablodiste.datastore.adapters.room.RoomListSourceOfTruth
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.models.room.People

@Dao
abstract class PeopleSourceOfTruth: RoomListSourceOfTruth<NoKey, People>("people", SampleApplication.roomDb,
    stalenessPolicy = DeleteAllNotInFetchStalenessPolicy { people -> people.id } // Example of staleness settings.
) {
    override fun query(key: NoKey): String = ""
}