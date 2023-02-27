package dev.pablodiste.datastore.sample.repositories.store.room;

import androidx.room.Dao
import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.adapters.retrofit.RetrofitFetcher
import dev.pablodiste.datastore.adapters.retrofit.RetrofitSender
import dev.pablodiste.datastore.adapters.room.RoomListSourceOfTruth
import dev.pablodiste.datastore.adapters.room.RoomSourceOfTruth
import dev.pablodiste.datastore.crud.SimpleCrudStoreBuilder
import dev.pablodiste.datastore.crud.SimpleCrudStoreImpl
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

private fun provideService() = RetrofitManager.dummyJSONService

fun provideDummyPostsStore(): SimpleStoreImpl<NoKey, List<DummyPost>> {
    return SimpleStoreBuilder.from(
        fetcher = RetrofitFetcher.of(provideService()) { _, service: DummyJsonService -> service.getPosts().posts },
        sourceOfTruth = SampleApplication.roomDb.dummyPostsSourceOfTruth()
    ).build()
}

data class DummyPostId(val id: Int)

fun provideDummyPostStore(): SimpleCrudStoreImpl<DummyPostId, DummyPost> {
    return SimpleCrudStoreBuilder.from(
        crudFetcher = CrudFetcher(
            readFetcher = RetrofitFetcher.of(provideService()) { key: DummyPostId, service: DummyJsonService -> service.getPost(key.id) },
            createSender = RetrofitSender.of(provideService()) { key: DummyPostId, entity: DummyPost, s: DummyJsonService -> s.createPost(entity) },
            updateSender = RetrofitSender.of(provideService()) { key: DummyPostId, entity: DummyPost, s: DummyJsonService -> s.updatePost(key.id, entity) },
            deleteSender = RetrofitSender.noResult(provideService()) { key: DummyPostId, entity: DummyPost, s: DummyJsonService -> s.deletePost(key.id) }
        ),
        sourceOfTruth = SampleApplication.roomDb.dummyPostSourceOfTruth(),
        keyBuilder = { post -> DummyPostId(post.id) }
    ).build()
}
