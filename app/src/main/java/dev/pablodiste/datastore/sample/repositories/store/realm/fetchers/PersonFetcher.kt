package dev.pablodiste.datastore.sample.repositories.store.realm.fetchers

import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.StarWarsService
import dev.pablodiste.datastore.sample.repositories.store.realm.RealmPersonStore

class PersonFetcher(starWarsService: StarWarsService): RetrofitFetcher<RealmPersonStore.Key, People, StarWarsService>(starWarsService) {
    override suspend fun fetch(key: RealmPersonStore.Key, service: StarWarsService): People {
        val person = service.getPerson(key.id)
        person.parseId()
        return person
    }
}