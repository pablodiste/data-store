package dev.pablodiste.datastore

import app.cash.turbine.test
import dev.pablodiste.datastore.impl.SimpleCrudStoreImpl
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import dev.pablodiste.datastore.ratelimiter.FetchAlways
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
class CrudStoreImplTest: CoroutineTest() {

    data class Key(val id: Int)
    data class Entity(val id: Int, val name: String)

    private lateinit var store: CrudStore<Key, Entity>
    private lateinit var fetcher: CrudFetcher<Key, Entity>
    private lateinit var sourceOfTruth: SourceOfTruth<Key, Entity>

    @Before
    fun prepare() {
        fetcher = mock<CrudFetcher<Key, Entity>> {
            on { rateLimitPolicy } doReturn FetchAlways
            onBlocking { fetch(Key(1)) } doReturn FetcherResult.Data(Entity(1, "One"))
            onBlocking { fetch(Key(2)) } doReturn FetcherResult.Data(Entity(2, "Two"))
            onBlocking { create(Key(1000), Entity(1000, "Three")) } doReturn FetcherResult.Data(Entity(3, "Three"))
            onBlocking { create(Key(1001), Entity(1001, "Four")) } doReturn FetcherResult.Data(Entity(4, "Four"))
            onBlocking { update(Key(2), Entity(2, "Two Modified")) } doReturn FetcherResult.Data(Entity(2, "Two Modified"))
            onBlocking { delete(Key(2), Entity(2, "Two")) } doReturn FetcherResult.Success(true)
        }
        sourceOfTruth = object: InMemorySourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (key: Key, value: Entity) -> Boolean = { _, value -> value.id == key.id }
        }
    }

    @Test
    fun storeCreate() = runTest {
        store = SimpleCrudStoreImpl(this, fetcher, sourceOfTruth) { entity -> Key(entity.id) }
        val stream = store.stream(Key(1000), refresh = false)
        val streamNew = store.stream(Key(3), refresh = false)
        val three = Entity(1000, "Three")
        val four = Entity(1001, "Four")
        val created1 = store.create(Key(1000), three)
        assertEquals(created1, StoreResponse.Data(three, origin = ResponseOrigin.SOURCE_OF_TRUTH))

        val created2 = store.create(Key(1001), four)
        assertEquals(created2, StoreResponse.Data(four, origin = ResponseOrigin.SOURCE_OF_TRUTH))
        stream.test {
            val temporaryData = awaitItem() as StoreResponse.Data
            assertEquals(StoreResponse.Data(three, origin = ResponseOrigin.SOURCE_OF_TRUTH), temporaryData)
        }
        streamNew.test {
            val finalData = awaitItem() as StoreResponse.Data
            assertEquals(StoreResponse.Data(Entity(3, "Three"), origin = ResponseOrigin.SOURCE_OF_TRUTH), finalData)
        }
        val get4 = store.get(Key(4))
        assertEquals(StoreResponse.Data(Entity(4, "Four"), origin = ResponseOrigin.SOURCE_OF_TRUTH), get4)
        store.dispose()
    }

}