package dev.pablodiste.datastore.sample.repositories.store.room

import androidx.room.Dao
import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.adapters.room.DeleteAllNotInFetchStalenessPolicy
import dev.pablodiste.datastore.adapters.room.RoomListCache
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.impl.NoKeySimpleStore
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.RoomStarWarsService

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
    abstract class PeopleCache: RoomListCache<NoKey, People>("people", dev.pablodiste.datastore.sample.SampleApplication.roomDb,
        stalenessPolicy = DeleteAllNotInFetchStalenessPolicy { people -> people.id } // Example of staleness settings.
    ) {
        override fun query(key: NoKey): String = ""
    }

}