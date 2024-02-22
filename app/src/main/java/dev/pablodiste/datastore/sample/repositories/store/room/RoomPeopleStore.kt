package dev.pablodiste.datastore.sample.repositories.store.room

import dev.pablodiste.datastore.impl.NoKeySimpleStore
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.repositories.store.room.fetchers.PeopleFetcher
import dev.pablodiste.datastore.sample.repositories.store.room.dao.PeopleSourceOfTruth

class RoomPeopleStore(fetcher: PeopleFetcher, peopleSourceOfTruth: PeopleSourceOfTruth): NoKeySimpleStore<List<People>>(
    fetcher = fetcher,
    sourceOfTruth = peopleSourceOfTruth
)