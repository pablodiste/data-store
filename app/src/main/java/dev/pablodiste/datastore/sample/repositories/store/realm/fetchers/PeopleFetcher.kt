package dev.pablodiste.datastore.sample.repositories.store.realm.fetchers

import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.StarWarsService

class PeopleFetcher(starWarsService: StarWarsService): RetrofitFetcher<NoKey, List<People>, StarWarsService>(starWarsService) {
    override suspend fun fetch(key: NoKey, service: StarWarsService): List<People> {
        val people = service.getPeople()
        people.results.forEach { it.parseId() }
        return people.results
    }
}
