package com.pablodiste.android.sample.network

import com.pablodiste.android.sample.models.room.People
import retrofit2.http.GET
import retrofit2.http.Path

interface RoomStarWarsService {

    @GET("people/")
    suspend fun getPeople(): ApiListResponse<People>

    @GET("people/{id}/")
    suspend fun getPerson(@Path("id") id: String): People
}