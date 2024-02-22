package dev.pablodiste.datastore.sample.repositories.store.room.fetchers

import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.network.RoomStarWarsService
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPersonStore
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

class PersonFetcher(roomStarWarsService: RoomStarWarsService): RetrofitFetcher<RoomPersonStore.Key, People, RoomStarWarsService>(roomStarWarsService) {
    override suspend fun fetch(key: RoomPersonStore.Key, service: RoomStarWarsService): People {
        val person = service.getPerson(key.id)
        person.parseId()
        return person
    }
}

class PersonFetcherWithError(roomStarWarsService: RoomStarWarsService): RetrofitFetcher<RoomPersonStore.Key, People, RoomStarWarsService>(roomStarWarsService) {
    override suspend fun fetch(key: RoomPersonStore.Key, service: RoomStarWarsService): People {
        throw HttpException(Response.error<String>(500, "Server error".toResponseBody()))
    }
}