package dev.pablodiste.datastore.sample.repositories.store.room;

import androidx.room.Dao
import dev.pablodiste.datastore.ChangeOperation
import dev.pablodiste.datastore.CrudFetcher2
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

fun provideDummyPostsStore(): SimpleStoreImpl<NoKey, List<DummyPost>> {
    return SimpleStoreBuilder.from(
        fetcher = RetrofitFetcher.of(RetrofitManager) { _, service: DummyJsonService -> service.getPosts().posts },
        sourceOfTruth = SampleApplication.roomDb.dummyPostsSourceOfTruth()
    ).build()
}

data class DummyPostId(val id: Int)

fun provideDummyPostStore(): SimpleCrudStoreImpl<DummyPostId, DummyPost> {
    return SimpleCrudStoreBuilder.from(
        crudFetcher = CrudFetcher2(
            readFetcher = RetrofitFetcher.of(RetrofitManager) { key: DummyPostId, service: DummyJsonService -> service.getPost(key.id) },
            createSender = RetrofitSender.of(RetrofitManager) { key: DummyPostId, entity: DummyPost, s: DummyJsonService, op: ChangeOperation ->
                s.createPost(entity)
            },
            updateSender = RetrofitSender.of(RetrofitManager) { key: DummyPostId, entity: DummyPost, s: DummyJsonService, op: ChangeOperation ->
                s.updatePost(key.id, entity)
            },
            deleteSender = RetrofitSender.noResult(RetrofitManager) { key: DummyPostId, entity: DummyPost, s: DummyJsonService, op: ChangeOperation ->
                s.deletePost(key.id)
            }
        ),
        sourceOfTruth = SampleApplication.roomDb.dummyPostSourceOfTruth(),
        keyBuilder = { post -> DummyPostId(post.id) }
    ).build()
}
