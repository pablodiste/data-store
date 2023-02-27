package dev.pablodiste.datastore.sample.repositories.store.realm

import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.adapters.realm.RealmSourceOfTruth
import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.closable.ScopedSimpleStoreImpl
import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.StarWarsService
import io.realm.RealmQuery

class RealmPersonStore: ScopedSimpleStoreImpl<RealmPersonStore.Key, People>(
    fetcher = PersonFetcher(),
    sourceOfTruth = PersonSourceOfTruth()
) {

    data class Key(val id: String)

    class PersonFetcher: RetrofitFetcher<Key, People, StarWarsService>(RetrofitManager.starWarsService) {
        override suspend fun fetch(key: Key, service: StarWarsService): People {
            val person = service.getPerson(key.id)
            person.parseId()
            return person
        }
    }

    class PersonSourceOfTruth: RealmSourceOfTruth<Key, People>(People::class.java) {
        override fun query(key: Key): (query: RealmQuery<People>) -> Unit = { it.equalTo("id", key.id) }
    }
}