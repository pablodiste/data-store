package dev.pablodiste.datastore

import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.impl.limit
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource

@ExperimentalCoroutinesApi
@OptIn(ExperimentalTime::class)
class FetcherLimiterTest: CoroutineTest() {

    data class Key(val id: Int)
    data class Entity(val id: Int, val name: String)

    private lateinit var store: Store<Key, Entity>
    private lateinit var mockFetcher: Fetcher<Key, Entity>
    private lateinit var fetcher: Fetcher<Key, Entity>
    private lateinit var sourceOfTruth: SourceOfTruth<Key, Entity>
    private val timeSource = TestTimeSource()

    @Before
    fun prepare() {
        mockFetcher = mock {
            onBlocking { fetch(any()) } doReturn successResult()
        }
        fetcher = mockFetcher.limit(RateLimitPolicy.FixedWindowPolicy(duration = 1.seconds, eventCount = 2, timeSource = timeSource))
        sourceOfTruth = object: InMemorySourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (value: Entity) -> Boolean = { value -> value.id == key.id }
        }
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
    }

    @Test
    fun normalFetch() = runTest {
        val result = store.fetch(Key(id = 1))
        assertNotNull(result)
        assertTrue(result is StoreResponse.Data)
    }

    @Test
    fun threeFetch() = runTest {
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.Data)
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.Data)
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.NoData)
    }

    @Test
    fun fourFetch() = runTest {
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.Data)
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.Data)
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.NoData)
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.NoData)
    }

    @Test
    fun threeFetchAndWait() = runTest {
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.Data)
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.Data)
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.NoData)
        timeSource.plusAssign(2.seconds)
        assertTrue(store.fetch(Key(id = 1)) is StoreResponse.Data)
    }

    private fun successResult() = FetcherResult.Data(Entity(1, "One"))
    private fun serverError() = FetcherResult.Error(FetcherError.HttpError(Exception("Server Error"), 500, "Server Error"))
    private fun customError() = FetcherResult.Error(FetcherError.HttpError(Exception("Custom Server Error"), 503, "Custom Server Error"))

}