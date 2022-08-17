package dev.pablodiste.datastore.sample.network

import dev.pablodiste.datastore.sample.models.realm.People
import dev.pablodiste.datastore.sample.models.realm.Planet
import retrofit2.http.GET
import retrofit2.http.Path

interface StarWarsService {

    @GET("people/")
    suspend fun getPeople(): ApiListResponse<People>

    @GET("people/{id}/")
    suspend fun getPerson(@Path("id") id: String): People

    @GET("planets/")
    suspend fun getPlanets(): ApiListResponse<Planet>
}