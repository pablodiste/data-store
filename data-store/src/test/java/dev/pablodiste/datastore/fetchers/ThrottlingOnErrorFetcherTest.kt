package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource

@ExperimentalCoroutinesApi
@OptIn(ExperimentalTime::class)
class ThrottlingOnErrorFetcherTest: CoroutineTest() {

    private lateinit var store: Store<TestKey, TestEntity>
    private lateinit var mockFetcher: Fetcher<TestKey, TestEntity>
    private lateinit var fetcher: Fetcher<TestKey, TestEntity>
    private lateinit var sourceOfTruth: SourceOfTruth<TestKey, TestEntity>
    private val timeSource = TestTimeSource()

    @Before
    fun prepare() {
        mockFetcher = mock()
        fetcher = mockFetcher.throttleOnError(maxErrorCount = 3, errorWindowDuration = 1.minutes, timeSource = timeSource)
        sourceOfTruth = object: InMemorySourceOfTruth<TestKey, TestEntity>() {
            override fun predicate(key: TestKey): (value: TestEntity) -> Boolean = { value -> value.id == key.id }
        }
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
    }

    @Test
    fun normalFetch() = runTest {
        whenever(mockFetcher.fetch(any()))
            .thenReturn(successResult())
        val result = store.fetch(TestKey(id = 1))
        assertNotNull(result)
        assertTrue(result is StoreResponse.Data)
    }

    @Test
    fun errorsLowerThanLimit() = runTest {
        whenever(mockFetcher.fetch(any()))
            .thenReturn(serverError())
        store.fetch(TestKey(id = 1))
        store.fetch(TestKey(id = 1))
        val result = store.fetch(TestKey(id = 1))
        assertNotNull(result)
        assertTrue(result is StoreResponse.Error && result.error !is ThrottlingError)
    }

    @Test
    fun throttled() = runTest {
        whenever(mockFetcher.fetch(any()))
            .thenReturn(serverError())
        store.fetch(TestKey(id = 1))
        store.fetch(TestKey(id = 1))
        store.fetch(TestKey(id = 1))
        val result = store.fetch(TestKey(id = 1))
        assertNotNull(result)
        assertTrue(result is StoreResponse.Error && result.error is FetcherException && (result.error as FetcherException).fetcherError.exception is ThrottlingError)
    }

    @Test
    fun notThrottledIfNotEnoughInWindow() = runTest {
        whenever(mockFetcher.fetch(any())).thenReturn(serverError())
        store.fetch(TestKey(id = 1))
        store.fetch(TestKey(id = 1))
        timeSource.plusAssign(2.minutes)
        store.fetch(TestKey(id = 1))
        val result = store.fetch(TestKey(id = 1))
        assertNotNull(result)
        assertTrue(result is StoreResponse.Error && result.error !is ThrottlingError)
    }
}