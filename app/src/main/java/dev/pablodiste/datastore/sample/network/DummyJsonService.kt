package dev.pablodiste.datastore.sample.network

import dev.pablodiste.datastore.sample.models.room.DummyPost
import retrofit2.http.*

interface DummyJsonService {

    @GET("posts/")
    suspend fun getPosts(): PagedPostsResponse

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): DummyPost

    @POST("posts/")
    suspend fun createPost(@Body post: DummyPost): DummyPost

    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") id: Int, @Body post: DummyPost): DummyPost

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Int)
}

data class PagedPostsResponse(
    val posts: List<DummyPost>,
    val total: Int,
    val skip: Int,
    val limit: Int
)