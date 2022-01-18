package com.pablodiste.android.sample.network

import com.pablodiste.android.sample.models.room.Post
import retrofit2.http.GET

interface JsonPlaceholderService {

    @GET("posts/")
    suspend fun getPosts(): List<Post>

}