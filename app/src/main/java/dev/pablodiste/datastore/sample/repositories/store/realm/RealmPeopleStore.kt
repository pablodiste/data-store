package dev.pablodiste.datastore.sample.repositories.store.realm

import android.util.Log
import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.adapters.realm.RealmListSourceOfTruth
import dev.pablodiste.datastore.closable.NoKeyScopedSimpleStore
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.StarWarsService
import io.realm.RealmQuery

class RealmPeopleStore: NoKeyScopedSimpleStore<List<People>>(
    fetcher = PeopleFetcher(),
    sourceOfTruth = PeopleSourceOfTruth()
) {

    class PeopleFetcher: RetrofitFetcher<NoKey, List<People>, StarWarsService>(StarWarsService::class.java, RetrofitManager) {
        override suspend fun fetch(key: NoKey, service: StarWarsService): FetcherResult<List<People>> {
            val people = service.getPeople()
            people.results.forEach { it.parseId() }
            return FetcherResult.Data(people.results)
        }
    }

    class PeopleSourceOfTruth: RealmListSourceOfTruth<NoKey, People>(People::class.java) {
        override fun query(key: NoKey): (query: RealmQuery<People>) -> Unit = { }
    }

    fun something() {
        Log.d("Test", "Testing custom methods")
    }
}