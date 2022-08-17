package dev.pablodiste.datastore.sample.repositories.store.realm

import android.util.Log
import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.closable.NoKeyScopedSimpleStore
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.models.realm.Planet
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.StarWarsService
import io.realm.RealmQuery

class RealmPlanetsStore: NoKeyScopedSimpleStore<List<Planet>>(
    fetcher = PlanetFetcher(),
    cache = PlanetCache()
) {

    class PlanetFetcher: RetrofitFetcher<NoKey, List<Planet>, StarWarsService>(StarWarsService::class.java, RetrofitManager) {
        override suspend fun fetch(key: NoKey, service: StarWarsService): FetcherResult<List<Planet>> {
            val planets = service.getPlanets()
            return FetcherResult.Data(planets.results)
        }
    }

    class PlanetCache: dev.pablodiste.datastore.adapters.realm.SimpleRealmListCache<NoKey, Planet>(Planet::class.java) {
        override fun query(key: NoKey): (query: RealmQuery<Planet>) -> Unit = { }
    }

    fun something() {
        Log.d("Test", "Testing custom methods")
    }
}