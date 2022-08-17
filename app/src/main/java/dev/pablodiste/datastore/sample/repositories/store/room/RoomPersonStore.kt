package dev.pablodiste.datastore.sample.repositories.store.room

import androidx.room.Dao
import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.adapters.room.DeleteAllNotInFetchStalenessPolicy
import dev.pablodiste.datastore.adapters.room.RoomCache
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.RoomStarWarsService

class RoomPersonStore: SimpleStoreImpl<RoomPersonStore.Key, People>(
    fetcher = PersonFetcher(),
    cache = dev.pablodiste.datastore.sample.SampleApplication.roomDb.personCache()
) {

    data class Key(val id: String)

    class PersonFetcher: RetrofitFetcher<Key, People, RoomStarWarsService>(RoomStarWarsService::class.java, RetrofitManager) {
        override suspend fun fetch(key: Key, service: RoomStarWarsService): FetcherResult<People> {
            val person = service.getPerson(key.id)
            person.parseId()
            return FetcherResult.Data(person)
        }
    }

    @Dao
    abstract class PersonCache: RoomCache<Key, People>("people", dev.pablodiste.datastore.sample.SampleApplication.roomDb) {
        override fun query(key: Key): String = "id = ${key.id}"
    }
}