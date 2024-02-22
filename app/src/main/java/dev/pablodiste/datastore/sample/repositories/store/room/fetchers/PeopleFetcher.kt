package dev.pablodiste.datastore.sample.repositories.store.room.fetchers

import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.network.RoomStarWarsService

class PeopleFetcher(roomStarWarsService: RoomStarWarsService): RetrofitFetcher<NoKey, List<People>, RoomStarWarsService>(roomStarWarsService) {
    override suspend fun fetch(key: NoKey, service: RoomStarWarsService): List<People> {
        val people = service.getPeople()
        people.results.forEach { it.parseId() }
        return people.results
    }
}
