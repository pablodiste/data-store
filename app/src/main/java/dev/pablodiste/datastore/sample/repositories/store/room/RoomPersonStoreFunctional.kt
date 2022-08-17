package dev.pablodiste.datastore.sample.repositories.store.room

import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.Store
import dev.pablodiste.datastore.impl.LimitedFetcher
import dev.pablodiste.datastore.impl.SimpleStoreBuilder
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.RoomStarWarsService

fun providePersonStore(): Store<RoomPersonStore.Key, People> {
    return SimpleStoreBuilder.from(
        fetcher = LimitedFetcher.of({ key ->
            FetcherResult.Data(provideStarWarsService().getPerson(key.id).apply { parseId() })
        }),
        cache = dev.pablodiste.datastore.sample.SampleApplication.roomDb.personCache()
    ).build()
}

private fun provideStarWarsService() = RetrofitManager.createService(RoomStarWarsService::class.java)
