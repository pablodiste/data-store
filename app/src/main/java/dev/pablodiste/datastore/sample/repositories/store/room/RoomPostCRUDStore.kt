package dev.pablodiste.datastore.sample.repositories.store.room;

import androidx.room.Dao
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.adapters.room.RoomSourceOfTruth
import dev.pablodiste.datastore.impl.LimitedCrudFetcher
import dev.pablodiste.datastore.impl.SimpleDirectCrudStoreBuilder
import dev.pablodiste.datastore.impl.SimpleDirectCrudStoreImpl
import dev.pablodiste.datastore.sample.models.room.Post
import dev.pablodiste.datastore.sample.network.JsonPlaceholderService
import dev.pablodiste.datastore.sample.network.RetrofitManager

data class PostKey(val id: Int)

fun providePostsCRUDStore(): SimpleDirectCrudStoreImpl<PostKey, Post> {
    return SimpleDirectCrudStoreBuilder.from(
        fetcher = LimitedCrudFetcher.of(
            fetch = { post -> FetcherResult.Data(provideService().getPost(post.id)) },
            create = { key, post -> FetcherResult.Data(provideService().createPost(post)) },
            update = { key, post -> FetcherResult.Data(provideService().updatePost(key.id, post)) },
            delete = { key, post -> provideService().deletePost(key.id); FetcherResult.Success(true) },
        ),
        sourceOfTruth = dev.pablodiste.datastore.sample.SampleApplication.roomDb.postSourceOfTruth(),
        keyBuilder = { entity -> PostKey(entity.id) }
    ).build() as SimpleDirectCrudStoreImpl
}

private fun provideService() = RetrofitManager.createService(JsonPlaceholderService::class.java)

@Dao
abstract class PostSourceOfTruth: RoomSourceOfTruth<PostKey, Post>("posts", dev.pablodiste.datastore.sample.SampleApplication.roomDb) {
    override fun query(key: PostKey): String = "id = ${key.id}"
}
