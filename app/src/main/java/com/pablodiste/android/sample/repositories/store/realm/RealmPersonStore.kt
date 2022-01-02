package com.pablodiste.android.sample.repositories.store.realm

import com.pablodiste.android.adapters.retrofit.RetrofitFetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.adapters.realm.SimpleRealmCache
import com.pablodiste.android.datastore.closable.ScopedSimpleStoreImpl
import com.pablodiste.android.sample.models.realm.People
import com.pablodiste.android.sample.network.RetrofitManager
import com.pablodiste.android.sample.network.StarWarsService
import io.realm.RealmQuery

class RealmPersonStore: ScopedSimpleStoreImpl<RealmPersonStore.Key, People>(
    fetcher = PersonFetcher(),
    cache = PersonCache()
) {

    data class Key(val id: String)

    class PersonFetcher: RetrofitFetcher<Key, People, StarWarsService>(StarWarsService::class.java, RetrofitManager) {
        override suspend fun fetch(key: Key, service: StarWarsService): FetcherResult<People> {
            val person = service.getPerson(key.id)
            person.parseId()
            return FetcherResult.Data(person)
        }
    }

    class PersonCache: SimpleRealmCache<Key, People>(People::class.java) {
        override fun query(key: Key): (query: RealmQuery<People>) -> Unit = { it.equalTo("id", key.id) }
    }
}