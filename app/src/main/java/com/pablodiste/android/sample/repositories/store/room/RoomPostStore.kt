package com.pablodiste.android.sample.repositories.store.room;

import androidx.room.Dao
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.adapters.room.RoomListCache
import com.pablodiste.android.datastore.impl.LimitedFetcher
import com.pablodiste.android.datastore.impl.NoKey
import com.pablodiste.android.datastore.impl.SimpleStoreBuilder
import com.pablodiste.android.datastore.impl.SimpleStoreImpl
import com.pablodiste.android.datastore.ratelimiter.RateLimitPolicy
import com.pablodiste.android.sample.SampleApplication
import com.pablodiste.android.sample.models.room.Post
import com.pablodiste.android.sample.network.JsonPlaceholderService
import com.pablodiste.android.sample.network.RetrofitManager
import java.util.concurrent.TimeUnit

data class PostKey(val id: Int)

fun providePostsStore(): SimpleStoreImpl<NoKey, List<Post>> {
    return SimpleStoreBuilder.from(
        fetcher = LimitedFetcher.of({ FetcherResult.Data(provideJsonPlaceholderService().getPosts()) }, RateLimitPolicy(2, TimeUnit.SECONDS)),
        cache = SampleApplication.roomDb.postsCache()
    ).build() as SimpleStoreImpl
}

private fun provideJsonPlaceholderService() = RetrofitManager.createService(JsonPlaceholderService::class.java)

@Dao
abstract class PostsCache: RoomListCache<NoKey, Post>("posts", SampleApplication.roomDb) {
    override fun query(key: NoKey): String = ""
}
