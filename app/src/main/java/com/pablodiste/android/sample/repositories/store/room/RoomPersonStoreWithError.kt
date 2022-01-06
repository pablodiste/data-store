package com.pablodiste.android.sample.repositories.store.room

import com.pablodiste.android.adapters.retrofit.RetrofitFetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.impl.SimpleStoreImpl
import com.pablodiste.android.sample.SampleApplication
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.network.RetrofitManager
import com.pablodiste.android.sample.network.RoomStarWarsService
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

class RoomPersonStoreWithError: SimpleStoreImpl<RoomPersonStore.Key, People>(
    fetcher = PersonFetcher(),
    cache = SampleApplication.roomDb.personCache()
) {
    class PersonFetcher: RetrofitFetcher<RoomPersonStore.Key, People, RoomStarWarsService>(RoomStarWarsService::class.java, RetrofitManager) {
        override suspend fun fetch(key: RoomPersonStore.Key, service: RoomStarWarsService): FetcherResult<People> {
            throw HttpException(Response.error<String>(500, "Server error".toResponseBody()))
        }
    }
}