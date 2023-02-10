package dev.pablodiste.datastore.sourceoftruth

import app.cash.turbine.test
import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.fetchers.limit
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class InMemorySourceOfTruthTest: CoroutineTest() {

    data class Key(val id: Int)
    data class Entity(val id: Int, val name: String)

    private lateinit var store: Store<Key, Entity>
    private lateinit var fetcher: Fetcher<Key, Entity>

    @Before
    fun prepare() {
        val fetcher = mock<Fetcher<Key, Entity>> {
            onBlocking { fetch(Key(1)) } doReturn FetcherResult.Data(Entity(1, "First"))
            onBlocking { fetch(Key(2)) } doReturn FetcherResult.Data(Entity(2, "Second"))
        }.limit(RateLimitPolicy.FixedWindowPolicy(10.seconds))
        val sourceOfTruth = object: InMemorySourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (value: Entity) -> Boolean = { value -> value.id == key.id }
        }
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
    }

    @Test
    fun storeGet() = runTest {
        val result = store.get(Key(id = 1))
        assertNotNull(result)
    }

    @Test
    fun storeStream() = runTest {
        val result = store.stream(Key(id = 1), refresh = true).first()
        assertNotNull(result)
        assertEquals(1, result.requireData().id)
        val fetchResult = store.fetch(Key(2))
        assertNotNull(fetchResult)
        assertEquals(2, fetchResult.requireData().id)
        val getResult = store.get(Key(1))
        assertNotNull(getResult)
        assertEquals(1, getResult.requireData().id)
    }

    @Test
    fun storeStreamEmissions() = runTest {
        store.stream(Key(id = 1), refresh = true)
            .onEach { println("Origin: " + it.requireOrigin()) }
            .test {
                // The original stream method (refresh) will call the fetcher and we'll receive the result here.
                assertNotNull(awaitItem())
                // This fetch request is made to another id, but the same cached id 1 is returned after the storage of the item 2
                store.fetch(Key(2))
                assertNotNull(awaitItem())
                // We have configured a rate limiter so this call should not emit anything.
                store.fetch(Key(1))
                expectNoEvents()
                // When forcing the fetch, we should receive the emission
                store.fetch(Key(1), forced = true)
                assertNotNull(awaitItem())
            }

        // In this case the store already has an item with the 1 key. Stream with refresh should emit twice, one for the cache and
        // another one for the refreshed data when refresh = true.
        store.stream(Key(id = 1), refresh = true)
            .onEach {
                when (it) {
                    is StoreResponse.Data -> println("Origin: " + it.requireOrigin())
                    is StoreResponse.Error -> println("Received Error")
                    is StoreResponse.NoData -> println("Received No Data")
                }
            }
            .test {
                val sotItem = awaitItem()
                assertNotNull(sotItem)
                assertTrue(sotItem is StoreResponse.Data)
                val sotItem2 = awaitItem()
                assertNotNull(sotItem2)
                assertTrue(sotItem2 is StoreResponse.NoData)
            }
    }

}