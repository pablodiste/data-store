package dev.pablodiste.datastore.sample.repositories.store.room

import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.repositories.store.room.fetchers.PersonFetcher
import dev.pablodiste.datastore.sample.repositories.store.room.dao.PersonSourceOfTruth

class RoomPersonStore(personFetcher: PersonFetcher, sourceOfTruth: PersonSourceOfTruth):
    SimpleStoreImpl<RoomPersonStore.Key, People>(fetcher = personFetcher, sourceOfTruth = sourceOfTruth) {
    data class Key(val id: String)
}
