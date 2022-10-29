package dev.pablodiste.datastore.sample.repositories.store.room;

import androidx.room.Dao
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.adapters.room.RoomListSourceOfTruth
import dev.pablodiste.datastore.impl.LimitedFetcher
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.impl.SimpleStoreBuilder
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import dev.pablodiste.datastore.sample.models.room.Post
import dev.pablodiste.datastore.sample.network.JsonPlaceholderService
import dev.pablodiste.datastore.sample.network.RetrofitManager
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

fun providePostsStore(): SimpleStoreImpl<NoKey, List<Post>> {
    return SimpleStoreBuilder.from(
        fetcher = LimitedFetcher.of(
            fetch = { FetcherResult.Data(provideService().getPosts()) },
            rateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 2.seconds)),
        sourceOfTruth = dev.pablodiste.datastore.sample.SampleApplication.roomDb.postsSourceOfTruth()
    ).build()
}

private fun provideService() = RetrofitManager.createService(JsonPlaceholderService::class.java)

@Dao
abstract class PostsSourceOfTruth: RoomListSourceOfTruth<NoKey, Post>("posts", dev.pablodiste.datastore.sample.SampleApplication.roomDb) {
    override fun query(key: NoKey): String = ""
}
