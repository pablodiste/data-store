package dev.pablodiste.datastore.sample.repositories.store.room

import dev.pablodiste.datastore.fetchers.throttleAllStoresOnError
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.repositories.store.room.fetchers.PersonFetcherWithError
import dev.pablodiste.datastore.sample.repositories.store.room.dao.PersonSourceOfTruth

class RoomPersonStoreWithError(personStoreWithError: PersonFetcherWithError, personSourceOfTruth: PersonSourceOfTruth):
    SimpleStoreImpl<RoomPersonStore.Key, People>(
    fetcher = personStoreWithError.throttleAllStoresOnError(),
    sourceOfTruth = personSourceOfTruth
) {

}