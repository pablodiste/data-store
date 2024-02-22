package dev.pablodiste.datastore.sample.repositories.store.realm

import dev.pablodiste.datastore.closable.ScopedSimpleStoreImpl
import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.repositories.store.realm.dao.PersonSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.realm.fetchers.PersonFetcher

class RealmPersonStore(personFetcher: PersonFetcher, personSourceOfTruth: PersonSourceOfTruth): ScopedSimpleStoreImpl<RealmPersonStore.Key, People>(
    fetcher = personFetcher,
    sourceOfTruth = personSourceOfTruth
) {
    data class Key(val id: String)
}