package com.pablodiste.android.sample.repositories.store

import android.util.Log
import com.pablodiste.android.adapters.retrofit.RetrofitFetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.adapters.realm.SimpleRealmListCache
import com.pablodiste.android.datastore.closable.NoKeyScopedSimpleStore
import com.pablodiste.android.datastore.impl.NoKey
import com.pablodiste.android.sample.models.realm.People
import com.pablodiste.android.sample.network.RetrofitManager
import com.pablodiste.android.sample.network.StarWarsService
import io.realm.RealmQuery
import kotlinx.coroutines.CoroutineScope

class PeopleStore(coroutineScope: CoroutineScope): NoKeyScopedSimpleStore<List<People>>(
    fetcher = PeopleFetcher(),
    cache = PeopleCache(),
    coroutineContext = coroutineScope.coroutineContext
) {

    class PeopleFetcher: RetrofitFetcher<NoKey, List<People>, StarWarsService>(StarWarsService::class.java, RetrofitManager) {
        override suspend fun fetch(key: NoKey, service: StarWarsService): FetcherResult<List<People>> {
            val people = service.getPeople()
            return FetcherResult.Data(people.results)
        }
    }

    class PeopleCache: SimpleRealmListCache<NoKey, People>(People::class.java) {
        override fun query(key: NoKey): (query: RealmQuery<People>) -> Unit = { }
    }

    fun something() {
        Log.d("Test", "Testing custom methods")
    }
}