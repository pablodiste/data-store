package dev.pablodiste.datastore.sample.repositories.store.realm

import android.util.Log
import dev.pablodiste.datastore.closable.NoKeyScopedSimpleStore
import dev.pablodiste.datastore.sample.models.realm.Planet
import dev.pablodiste.datastore.sample.repositories.store.realm.dao.PlanetSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.realm.fetchers.PlanetsFetcher

class RealmPlanetsStore(planetsFetcher: PlanetsFetcher, planetSourceOfTruth: PlanetSourceOfTruth): NoKeyScopedSimpleStore<List<Planet>>(
    fetcher = planetsFetcher,
    sourceOfTruth = planetSourceOfTruth
) {
    fun something() {
        Log.d("Test", "Testing custom methods")
    }
}