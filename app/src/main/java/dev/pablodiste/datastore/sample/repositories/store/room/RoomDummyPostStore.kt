package dev.pablodiste.datastore.sample.repositories.store.room;

import androidx.room.Dao
import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.adapters.room.RoomListSourceOfTruth
import dev.pablodiste.datastore.adapters.room.RoomSourceOfTruth
import dev.pablodiste.datastore.impl.NoKey
import dev.pablodiste.datastore.impl.SimpleStoreBuilder
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.sample.SampleApplication
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.network.DummyJsonService
import dev.pablodiste.datastore.sample.network.RetrofitManager

@Dao
abstract class DummyPostsSourceOfTruth: RoomListSourceOfTruth<NoKey, DummyPost>("dummy_posts", SampleApplication.roomDb) {
    override fun query(key: NoKey): String = ""
}

@Dao
abstract class DummyPostSourceOfTruth: RoomSourceOfTruth<DummyPostId, DummyPost>("dummy_posts", SampleApplication.roomDb) {
    override fun query(key: DummyPostId): String = "id = ${key.id}"
}

fun provideDummyPostsStore(): SimpleStoreImpl<NoKey, List<DummyPost>> {
    return SimpleStoreBuilder.from(
        fetcher = RetrofitFetcher.of(RetrofitManager) { _, service: DummyJsonService -> service.getPosts().posts },
        sourceOfTruth = SampleApplication.roomDb.dummyPostsSourceOfTruth()
    ).build()
}

data class DummyPostId(val id: Int)
fun provideDummyPostStore(): SimpleStoreImpl<DummyPostId, DummyPost> {
    return SimpleStoreBuilder.from(
        fetcher = RetrofitFetcher.of(RetrofitManager) { key: DummyPostId, service: DummyJsonService -> service.getPost(key.id) },
        sourceOfTruth = SampleApplication.roomDb.dummyPostSourceOfTruth()
    ).build()
}
