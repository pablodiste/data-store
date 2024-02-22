package dev.pablodiste.datastore.sample.repositories.store.realm.dao

import dev.pablodiste.datastore.adapters.realm.RealmSourceOfTruth
import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.repositories.store.realm.RealmPersonStore
import io.realm.RealmQuery

class PersonSourceOfTruth: RealmSourceOfTruth<RealmPersonStore.Key, People>(People::class.java) {
    override fun query(key: RealmPersonStore.Key): (query: RealmQuery<People>) -> Unit = { it.equalTo("id", key.id) }
}