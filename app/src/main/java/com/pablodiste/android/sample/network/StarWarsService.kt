package com.pablodiste.android.sample.network

import com.pablodiste.android.sample.models.realm.People
import retrofit2.http.GET

interface StarWarsService {

    @GET("people/")
    suspend fun getPeople(): ApiListResponse<People>
}