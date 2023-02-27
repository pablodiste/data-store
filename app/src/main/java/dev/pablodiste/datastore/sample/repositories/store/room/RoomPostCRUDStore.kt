package dev.pablodiste.datastore.sample.repositories.store.room;

import androidx.room.Dao
import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.adapters.room.RoomSourceOfTruth
import dev.pablodiste.datastore.crud.SimpleCrudStoreBuilder
import dev.pablodiste.datastore.crud.SimpleCrudStoreImpl
import dev.pablodiste.datastore.sample.models.room.Post
import dev.pablodiste.datastore.sample.network.JsonPlaceholderService
import dev.pablodiste.datastore.sample.network.RetrofitManager

data class PostKey(val id: Int)

fun providePostsCRUDStore(): SimpleCrudStoreImpl<PostKey, Post> {
    return SimpleCrudStoreBuilder.from(
        crudFetcher = CrudFetcher(
            readFetcher = { post -> FetcherResult.Data(provideService().getPost(post.id)) },
            createSender = { key, post -> FetcherResult.Data(provideService().createPost(post)) },
            updateSender = { key, post -> FetcherResult.Data(provideService().updatePost(key.id, post)) },
            deleteSender = { key, post -> provideService().deletePost(key.id); FetcherResult.Success(true) },
        ),
        sourceOfTruth = dev.pablodiste.datastore.sample.SampleApplication.roomDb.postSourceOfTruth(),
        keyBuilder = { entity -> PostKey(entity.id) }
    ).build()
}

private fun provideService() = RetrofitManager.jsonPlaceholderService

@Dao
abstract class PostSourceOfTruth: RoomSourceOfTruth<PostKey, Post>("posts", dev.pablodiste.datastore.sample.SampleApplication.roomDb) {
    override fun query(key: PostKey): String = "id = ${key.id}"
}
