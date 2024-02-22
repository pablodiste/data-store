package dev.pablodiste.datastore.sample.repositories.store.realm

import android.util.Log
import dev.pablodiste.datastore.closable.NoKeyScopedSimpleStore
import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.repositories.store.realm.dao.PeopleSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.realm.fetchers.PeopleFetcher

class RealmPeopleStore(peopleFetcher: PeopleFetcher, peopleSourceOfTruth: PeopleSourceOfTruth): NoKeyScopedSimpleStore<List<People>>(
    fetcher = peopleFetcher,
    sourceOfTruth = peopleSourceOfTruth
) {
    fun something() {
        Log.d("Test", "Testing custom methods")
    }
}