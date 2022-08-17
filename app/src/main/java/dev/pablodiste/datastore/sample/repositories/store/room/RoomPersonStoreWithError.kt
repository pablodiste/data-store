package dev.pablodiste.datastore.sample.repositories.store.room

import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.RoomStarWarsService
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

class RoomPersonStoreWithError: SimpleStoreImpl<RoomPersonStore.Key, People>(
    fetcher = PersonFetcher(),
    cache = dev.pablodiste.datastore.sample.SampleApplication.roomDb.personCache()
) {
    class PersonFetcher: RetrofitFetcher<RoomPersonStore.Key, People, RoomStarWarsService>(RoomStarWarsService::class.java, RetrofitManager) {
        override suspend fun fetch(key: RoomPersonStore.Key, service: RoomStarWarsService): FetcherResult<People> {
            throw HttpException(Response.error<String>(500, "Server error".toResponseBody()))
        }
    }
}