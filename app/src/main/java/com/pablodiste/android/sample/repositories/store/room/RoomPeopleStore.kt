package com.pablodiste.android.sample.repositories.store.room

import androidx.room.Dao
import com.pablodiste.android.adapters.retrofit.RetrofitFetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.adapters.room.RoomListCache
import com.pablodiste.android.datastore.impl.NoKey
import com.pablodiste.android.datastore.impl.NoKeySimpleStore
import com.pablodiste.android.sample.SampleApplication
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.network.RetrofitManager
import com.pablodiste.android.sample.network.RoomStarWarsService

class RoomPeopleStore: NoKeySimpleStore<List<People>>(
    fetcher = PeopleFetcher(),
    cache = SampleApplication.roomDb.peopleCache()
) {

    class PeopleFetcher: RetrofitFetcher<NoKey, List<People>, RoomStarWarsService>(RoomStarWarsService::class.java, RetrofitManager) {
        override suspend fun fetch(key: NoKey, service: RoomStarWarsService): FetcherResult<List<People>> {
            val people = service.getPeople()
            people.results.forEach { it.parseId() }
            return FetcherResult.Data(people.results)
        }
    }

    @Dao
    abstract class PeopleCache: RoomListCache<NoKey, People>("people", SampleApplication.roomDb) {
        override fun query(key: NoKey): String = ""
    }

}