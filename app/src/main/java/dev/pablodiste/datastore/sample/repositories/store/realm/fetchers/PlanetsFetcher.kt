package dev.pablodiste.datastore.sample.repositories.store.realm.fetchers

import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.models.realm.Planet
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.StarWarsService

class PlanetsFetcher: RetrofitFetcher<NoKey, List<Planet>, StarWarsService>(RetrofitManager.starWarsService) {
    override suspend fun fetch(key: NoKey, service: StarWarsService): List<Planet> {
        val planets = service.getPlanets()
        return planets.results
    }
}