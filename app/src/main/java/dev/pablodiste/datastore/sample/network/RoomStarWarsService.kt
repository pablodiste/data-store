package dev.pablodiste.datastore.sample.network

import dev.pablodiste.datastore.sample.models.room.People
import retrofit2.http.GET
import retrofit2.http.Path

interface RoomStarWarsService {

    @GET("people/")
    suspend fun getPeople(): ApiListResponse<People>

    @GET("people/{id}/")
    suspend fun getPerson(@Path("id") id: String): People
}