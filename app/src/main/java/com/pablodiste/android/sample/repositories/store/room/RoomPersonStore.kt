package com.pablodiste.android.sample.repositories.store.room

import androidx.room.Dao
import com.pablodiste.android.adapters.retrofit.RetrofitFetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.adapters.room.DeleteAllNotInFetchStalenessPolicy
import com.pablodiste.android.datastore.adapters.room.RoomCache
import com.pablodiste.android.datastore.impl.SimpleStoreImpl
import com.pablodiste.android.sample.SampleApplication
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.network.RetrofitManager
import com.pablodiste.android.sample.network.RoomStarWarsService

class RoomPersonStore: SimpleStoreImpl<RoomPersonStore.Key, People>(
    fetcher = PersonFetcher(),
    cache = SampleApplication.roomDb.personCache()
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
    abstract class PersonCache: RoomCache<Key, People>("people", SampleApplication.roomDb) {
        override fun query(key: Key): String = "id = ${key.id}"
    }
}