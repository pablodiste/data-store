package dev.pablodiste.datastore.sample.network

import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.models.room.Starship
import dev.pablodiste.datastore.sample.models.room.StarshipDTO
import retrofit2.http.GET
import retrofit2.http.Path

interface RoomStarWarsService {

    @GET("people/")
    suspend fun getPeople(): ApiListResponse<People>

    @GET("people/{id}/")
    suspend fun getPerson(@Path("id") id: String): People

    @GET("starships/")
    suspend fun getStarships(): ApiListResponse<StarshipDTO>

}