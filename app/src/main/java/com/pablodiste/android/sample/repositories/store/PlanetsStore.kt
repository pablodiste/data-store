package com.pablodiste.android.sample.repositories.store

import android.util.Log
import com.pablodiste.android.adapters.retrofit.RetrofitFetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.adapters.realm.SimpleRealmListCache
import com.pablodiste.android.datastore.closable.NoKeyScopedSimpleStore
import com.pablodiste.android.datastore.impl.NoKey
import com.pablodiste.android.sample.models.realm.People
import com.pablodiste.android.sample.models.realm.Planet
import com.pablodiste.android.sample.network.RetrofitManager
import com.pablodiste.android.sample.network.StarWarsService
import io.realm.RealmQuery
import kotlinx.coroutines.CoroutineScope

class PlanetsStore: NoKeyScopedSimpleStore<List<Planet>>(
    fetcher = PlanetFetcher(),
    cache = PlanetCache()
) {

    class PlanetFetcher: RetrofitFetcher<NoKey, List<Planet>, StarWarsService>(StarWarsService::class.java, RetrofitManager) {
        override suspend fun fetch(key: NoKey, service: StarWarsService): FetcherResult<List<Planet>> {
            val planets = service.getPlanets()
            return FetcherResult.Data(planets.results)
        }
    }

    class PlanetCache: SimpleRealmListCache<NoKey, Planet>(Planet::class.java) {
        override fun query(key: NoKey): (query: RealmQuery<Planet>) -> Unit = { }
    }

    fun something() {
        Log.d("Test", "Testing custom methods")
    }
}