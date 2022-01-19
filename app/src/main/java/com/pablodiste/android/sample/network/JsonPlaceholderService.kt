package com.pablodiste.android.sample.network

import com.pablodiste.android.sample.models.room.Post
import retrofit2.http.*

interface JsonPlaceholderService {

    @GET("posts/")
    suspend fun getPosts(): List<Post>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post

    @POST("posts/")
    suspend fun createPost(@Body post: Post): Post

    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") id: Int, @Body post: Post): Post

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Int): Boolean
}