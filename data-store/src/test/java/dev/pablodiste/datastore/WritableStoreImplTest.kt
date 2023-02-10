package dev.pablodiste.datastore

import app.cash.turbine.test
import dev.pablodiste.datastore.impl.SimpleStoreBuilder
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import dev.pablodiste.datastore.writable.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
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
        fetcher = mock {
            onBlocking { fetch(Key(1)) } doReturn FetcherResult.Data(Entity(1, "One"))
            onBlocking { fetch(Key(2)) } doReturn FetcherResult.Data(Entity(2, "Two"))
        }
        sourceOfTruth = object: InMemorySourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (value: Entity) -> Boolean = { value -> value.id == key.id }
        }
    }

     @After
     fun after() {
         WorkerManager.dispose()
     }

    @Test
    fun storeCreate() = runTest {
        sender = mock {
            onBlocking { send(Key(1000), Entity(1000, "Three")) } doReturn
                    FetcherResult.Data(Entity(3, "Three"))
        }
        store = SimpleWritableStoreBuilder.from(this, fetcher, sender, sourceOfTruth) { entity -> Key(entity.id) }.build()
        val three = Entity(1000, "Three")

        store.stream(StoreRequest(key = Key(3), refresh = false, fetchWhenNoDataFound = false)).test {
            val created = store.create(Key(1000), three)
            assertEquals(StoreResponse.Data(three, origin = ResponseOrigin.SOURCE_OF_TRUTH), created)

            val createdFromAPI = awaitItem()
            assertEquals(StoreResponse.Data(Entity(3, "Three"), origin = ResponseOrigin.SOURCE_OF_TRUTH), createdFromAPI)

            cancelAndIgnoreRemainingEvents()
        }
        WorkerManager.dispose()
    }

    @Test
    fun storeUpdate() = runTest {
        sender = mock {
            onBlocking { send(Key(2), Entity(2, "Two")) } doReturn
                    FetcherResult.Data(Entity(2, "Two Updated"))
        }
        store = SimpleWritableStoreBuilder.from(this, fetcher, sender, sourceOfTruth) { entity -> Key(entity.id) }.build()
        store.stream(Key(2), refresh = false).test {
            val fetchFromStream = awaitItem() as StoreResponse.Data
            assertEquals(StoreResponse.Data(Entity(2, "Two"), origin = ResponseOrigin.FETCHER), fetchFromStream)

            store.update(Key(2), fetchFromStream.requireData()) { it.copy(name = "Two Updated") }
            val update = awaitItem() as StoreResponse.Data
            assertEquals(StoreResponse.Data(Entity(2, "Two Updated"), origin = ResponseOrigin.SOURCE_OF_TRUTH), update)

            cancelAndIgnoreRemainingEvents()
        }
        WorkerManager.dispose()
    }


    @Test
    fun storeDelete() = runTest {
        sender = mock {
            onBlocking { send(Key(2), Entity(2, "Two")) } doReturn FetcherResult.Success(true)
        }
        store = SimpleWritableStoreBuilder.from(this, fetcher, sender, sourceOfTruth) { entity -> Key(entity.id) }.build()
        store.stream(Key(2), refresh = false).test {
            val fetchFromStream = awaitItem() as StoreResponse.Data
            assertEquals(StoreResponse.Data(Entity(2, "Two"), origin = ResponseOrigin.FETCHER), fetchFromStream)

            store.remove(Key(2), fetchFromStream.requireData())
            expectNoEvents()
        }
        WorkerManager.dispose()
    }

    @Test
    fun storeReflectsPendingChangesWhenGrouping() = runTest {
        // Preparation
        sender = object: Sender<Key, Entity> {
            override suspend fun send(key: Key, entity: Entity): FetcherResult<Entity> {
                delay(1000000)
                return FetcherResult.Data(Entity(2, "Two Updated"))
            }
        }
        val writableStore = SimpleWritableStoreBuilder.from(this, fetcher, sender, sourceOfTruth) { entity -> Key(entity.id) }.build()
        val queryStore = SimpleStoreBuilder.from(fetcher, sourceOfTruth).build()

        // Connects two stores, updates done in the write store will be reflected in the other if they are pending
        groupStoresByEntity(writableStore, queryStore)

        writableStore.stream(Key(2), refresh = false).test {
            val fetchFromStream = awaitItem() as StoreResponse.Data
            assertEquals(StoreResponse.Data(Entity(2, "Two"), origin = ResponseOrigin.FETCHER), fetchFromStream)

            // This update takes a long time to complete, simulating slow server
            writableStore.update(Key(2), fetchFromStream.requireData()) { it.copy(name = "Two Updated") }
            // If we fetch data from another store, the updated data is applied to the fetched data.
            val fetched = queryStore.fetch(Key(2))
            assertEquals(StoreResponse.Data(Entity(2, "Two Updated"), origin = ResponseOrigin.FETCHER), fetched)

            val update = awaitItem() as StoreResponse.Data
            assertEquals(StoreResponse.Data(Entity(2, "Two Updated"), origin = ResponseOrigin.SOURCE_OF_TRUTH), update)

            cancelAndIgnoreRemainingEvents()
        }
        WorkerManager.dispose()
    }
}