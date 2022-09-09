package dev.pablodiste.datastore

import app.cash.turbine.test
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import dev.pablodiste.datastore.ratelimiter.FetchAlways
import dev.pablodiste.datastore.writable.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
class WritableStoreImplTest: CoroutineTest() {

    data class Key(val id: Int)
    data class Entity(val id: Int, val name: String)

    private lateinit var store: WritableStore<Key, Entity>
    private lateinit var fetcher: Fetcher<Key, Entity>
    private lateinit var sender: Sender<Key, Entity>
    private lateinit var sourceOfTruth: SourceOfTruth<Key, Entity>

    @Before
    fun prepare() {
        fetcher = mock<Fetcher<Key, Entity>> {
            on { rateLimitPolicy } doReturn FetchAlways
            onBlocking { fetch(Key(1)) } doReturn FetcherResult.Data(Entity(1, "One"))
            onBlocking { fetch(Key(2)) } doReturn FetcherResult.Data(Entity(2, "Two"))
        }
        sender = mock {
            on { rateLimitPolicy } doReturn FetchAlways
            onBlocking { send(Key(1000), Entity(1000, "Three"), ChangeOperation.CREATE) } doReturn
                FetcherResult.Data(Entity(3, "Three"))
        }
        sourceOfTruth = object: InMemorySourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (key: Key, value: Entity) -> Boolean = { _, value -> value.id == key.id }
        }
    }

    @Test
    fun storeCreate() = runTest {
        store = SimpleWritableStoreBuilder.from(this, fetcher, sender, sourceOfTruth) { entity -> Key(entity.id) }.build()
        val stream = store.stream(Key(1000), refresh = false)
        val streamNew = store.stream(Key(3), refresh = false)
        val three = Entity(1000, "Three")

        val created = store.create(Key(1000), three)
        assertEquals(created, StoreResponse.Data(three, origin = ResponseOrigin.SOURCE_OF_TRUTH))

        stream.test {
            val temporaryData = awaitItem() as StoreResponse.Data
            assertEquals(StoreResponse.Data(three, origin = ResponseOrigin.SOURCE_OF_TRUTH), temporaryData)
        }
        streamNew.test {
            val finalData = awaitItem().requireData()
            assertEquals(StoreResponse.Data(Entity(3, "Three"), origin = ResponseOrigin.SOURCE_OF_TRUTH), finalData)
        }
        store.dispose()
    }

    @Test
    fun storeUpdate() = runTest {
        store = SimpleWritableStoreBuilder.from(this, fetcher, sender, sourceOfTruth) { entity -> Key(entity.id) }.build()
        store.stream(Key(2), refresh = false).test {
            val fetchFromStream = awaitItem() as StoreResponse.Data
            assertEquals(StoreResponse.Data(Entity(2, "Two"), origin = ResponseOrigin.FETCHER), fetchFromStream)

            store.update(Key(2), fetchFromStream.requireData()) { it.copy(name = "Two Updated") }
            val update = awaitItem() as StoreResponse.Data
            assertEquals(StoreResponse.Data(Entity(2, "Two Updated"), origin = ResponseOrigin.SOURCE_OF_TRUTH), update)
        }
        store.dispose()
    }

}