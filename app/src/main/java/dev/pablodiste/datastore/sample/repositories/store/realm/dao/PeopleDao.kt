package dev.pablodiste.datastore.sample.repositories.store.realm.dao

import dev.pablodiste.datastore.adapters.realm.RealmListSourceOfTruth
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.models.realm.People
import io.realm.RealmQuery

class PeopleSourceOfTruth: RealmListSourceOfTruth<NoKey, People>(People::class.java) {
    override fun query(key: NoKey): (query: RealmQuery<People>) -> Unit = { }
}