package dev.pablodiste.datastore.sample.repositories.store.room;

import androidx.room.Dao
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.adapters.room.RoomCache
import dev.pablodiste.datastore.impl.LimitedCrudFetcher
import dev.pablodiste.datastore.impl.SimpleCrudStoreBuilder
import dev.pablodiste.datastore.impl.SimpleCrudStoreImpl
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.models.room.Post
import dev.pablodiste.datastore.sample.network.JsonPlaceholderService
import dev.pablodiste.datastore.sample.network.RetrofitManager

data class PostKey(val id: Int)

fun providePostsCRUDStore(): SimpleCrudStoreImpl<PostKey, Post> {
    return SimpleCrudStoreBuilder.from(
        fetcher = LimitedCrudFetcher.of(
            fetch = { post -> FetcherResult.Data(provideService().getPost(post.id)) },
            create = { key, post -> FetcherResult.Data(provideService().createPost(post)) },
            update = { key, post -> FetcherResult.Data(provideService().updatePost(key.id, post)) },
            delete = { key, post -> provideService().deletePost(key.id); true },
        ),
        cache = dev.pablodiste.datastore.sample.SampleApplication.roomDb.postCache(),
        keyBuilder = { entity -> PostKey(entity.id) }
    ).build() as SimpleCrudStoreImpl
}

private fun provideService() = RetrofitManager.createService(JsonPlaceholderService::class.java)

@Dao
abstract class PostCache: RoomCache<PostKey, Post>("posts", dev.pablodiste.datastore.sample.SampleApplication.roomDb) {
    override fun query(key: PostKey): String = "id = ${key.id}"
}
