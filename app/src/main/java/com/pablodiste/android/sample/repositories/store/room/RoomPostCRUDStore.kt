package com.pablodiste.android.sample.repositories.store.room;

import androidx.room.Dao
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.adapters.room.RoomCache
import com.pablodiste.android.datastore.impl.LimitedCrudFetcher
import com.pablodiste.android.datastore.impl.SimpleCrudStoreBuilder
import com.pablodiste.android.datastore.impl.SimpleCrudStoreImpl
import com.pablodiste.android.sample.SampleApplication
import com.pablodiste.android.sample.models.room.Post
import com.pablodiste.android.sample.network.JsonPlaceholderService
import com.pablodiste.android.sample.network.RetrofitManager

data class PostKey(val id: Int)

fun providePostsCRUDStore(): SimpleCrudStoreImpl<PostKey, Post> {
    return SimpleCrudStoreBuilder.from(
        fetcher = LimitedCrudFetcher.of(
            fetch = { post -> FetcherResult.Data(provideService().getPost(post.id)) },
            create = { key, post -> FetcherResult.Data(provideService().createPost(post)) },
            update = { key, post -> FetcherResult.Data(provideService().updatePost(key.id, post)) },
            delete = { key, post -> provideService().deletePost(key.id); true },
        ),
        cache = SampleApplication.roomDb.postCache(),
        keyBuilder = { entity -> PostKey(entity.id) }
    ).build() as SimpleCrudStoreImpl
}

private fun provideService() = RetrofitManager.createService(JsonPlaceholderService::class.java)

@Dao
abstract class PostCache: RoomCache<PostKey, Post>("posts", SampleApplication.roomDb) {
    override fun query(key: PostKey): String = "id = ${key.id}"
}
