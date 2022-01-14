package com.pablodiste.android.sample.repositories.store.room

import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.Store
import com.pablodiste.android.datastore.impl.LimitedFetcher
import com.pablodiste.android.datastore.impl.SimpleStoreBuilder
import com.pablodiste.android.sample.SampleApplication
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.network.RetrofitManager
import com.pablodiste.android.sample.network.RoomStarWarsService

fun providePersonStore(): Store<RoomPersonStore.Key, People> {
    return SimpleStoreBuilder.from(
        fetcher = LimitedFetcher.of({ key ->
            FetcherResult.Data(provideStarWarsService().getPerson(key.id).apply { parseId() })
        }),
        cache = SampleApplication.roomDb.personCache()
    ).build()
}

private fun provideStarWarsService() = RetrofitManager.createService(RoomStarWarsService::class.java)
