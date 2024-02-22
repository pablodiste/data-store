package dev.pablodiste.datastore.sample.repositories.store.realm.dao

import dev.pablodiste.datastore.adapters.realm.RealmListSourceOfTruth
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.models.realm.Planet
import io.realm.RealmQuery

class PlanetSourceOfTruth: RealmListSourceOfTruth<NoKey, Planet>(Planet::class.java) {
    override fun query(key: NoKey): (query: RealmQuery<Planet>) -> Unit = { }
}