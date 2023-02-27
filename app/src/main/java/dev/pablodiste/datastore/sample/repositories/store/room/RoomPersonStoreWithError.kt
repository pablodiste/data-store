package dev.pablodiste.datastore.sample.repositories.store.room

import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.fetchers.throttleAllStoresOnError
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.RoomStarWarsService
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

class RoomPersonStoreWithError: SimpleStoreImpl<RoomPersonStore.Key, People>(
    fetcher = PersonFetcher().throttleAllStoresOnError(),
    sourceOfTruth = dev.pablodiste.datastore.sample.SampleApplication.roomDb.personSourceOfTruth()
) {
    class PersonFetcher: RetrofitFetcher<RoomPersonStore.Key, People, RoomStarWarsService>(RetrofitManager.roomStarWarsService) {
        override suspend fun fetch(key: RoomPersonStore.Key, service: RoomStarWarsService): People {
            throw HttpException(Response.error<String>(500, "Server error".toResponseBody()))
        }
    }
}