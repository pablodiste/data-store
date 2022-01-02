package com.pablodiste.android.sample.repositories.store.room

import androidx.room.Dao
import com.pablodiste.android.adapters.retrofit.RetrofitFetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.adapters.room.GenericListDAO
import com.pablodiste.android.datastore.adapters.room.SimpleRoomListCache
import com.pablodiste.android.datastore.impl.NoKey
import com.pablodiste.android.datastore.impl.NoKeySimpleStore
import com.pablodiste.android.sample.SampleApplication
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.network.RetrofitManager
import com.pablodiste.android.sample.network.RoomStarWarsService

class RoomPeopleStore: NoKeySimpleStore<List<People>>(
    fetcher = PeopleFetcher(),
    cache = PeopleCache(SampleApplication.roomDb.peopleCache())
) {

    class PeopleFetcher: RetrofitFetcher<NoKey, List<People>, RoomStarWarsService>(RoomStarWarsService::class.java, RetrofitManager) {
        override suspend fun fetch(key: NoKey, service: RoomStarWarsService): FetcherResult<List<People>> {
            val people = service.getPeople()
            people.results.forEach { it.parseId() }
            return FetcherResult.Data(people.results)
        }
    }

    class PeopleCache(dao: GenericListDAO<NoKey, People>): SimpleRoomListCache<NoKey, People>(dao)

    @Dao
    abstract class PeopleDao: GenericListDAO<NoKey, People>("people", SampleApplication.roomDb) {
        override fun query(key: NoKey): String = ""
    }

}