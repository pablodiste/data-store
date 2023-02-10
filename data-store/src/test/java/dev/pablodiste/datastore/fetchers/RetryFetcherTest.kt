package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import dev.pablodiste.datastore.retry.RetryPolicy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class RetryFetcherTest: CoroutineTest() {

    data class Key(val id: Int)
    data class Entity(val id: Int, val name: String)

    private lateinit var store: Store<Key, Entity>
    private lateinit var mockFetcher: Fetcher<Key, Entity>
    private lateinit var fetcher: Fetcher<Key, Entity>
    private lateinit var sourceOfTruth: SourceOfTruth<Key, Entity>

    @Before
    fun prepare() {
        mockFetcher = mock()
        fetcher = mockFetcher.retry(RetryPolicy.ExponentialBackoff(maxRetries = 2))
        sourceOfTruth = object: InMemorySourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (value: Entity) -> Boolean = { value -> value.id == key.id }
        }
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
    }

    @Test
    fun fetchWithAllErrors() = runTest {
        whenever(mockFetcher.fetch(any()))
            .thenReturn(serverError())
            .thenReturn(serverError())
            .thenReturn(serverError())
        val result = store.fetch(Key(id = 1))
        assertNotNull(result)
        assertTrue(result is StoreResponse.Error)
    }

    @Test
    fun fetchWithErrorsButThenWorks() = runTest {
        whenever(mockFetcher.fetch(any()))
            .thenReturn(serverError())
            .thenReturn(serverError())
            .thenReturn(successResult())
        val result = store.fetch(Key(id = 1))
        assertTrue(result is StoreResponse.Data)
    }

    @Test
    fun fetchRetry3() = runTest {
        fetcher = mockFetcher.retry(RetryPolicy.ExponentialBackoff(3))
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
        whenever(fetcher.fetch(any()))
            .thenReturn(serverError())
            .thenReturn(serverError())
            .thenReturn(serverError())
            .thenReturn(serverError())
        val result = store.fetch(Key(id = 1))
        assertTrue(result is StoreResponse.Error)
    }

    @Test
    fun fetchDoNotRetry() = runTest {
        fetcher = mockFetcher.retry(RetryPolicy.DoNotRetry)
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
        whenever(mockFetcher.fetch(any()))
            .thenReturn(serverError())
            .thenReturn(successResult())
        val result = store.fetch(Key(id = 1))
        assertTrue(result is StoreResponse.Error)
    }

    @Test
    fun fetchWithCustomHTTPError() = runTest {
        fetcher = mockFetcher.retry(RetryPolicy.ExponentialBackoff(maxRetries = 2, retryOnErrorCodes = listOf(503)))
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
        whenever(mockFetcher.fetch(any()))
            .thenReturn(customError())
            .thenReturn(customError())
            .thenReturn(successResult())
        val result = store.fetch(Key(id = 1))
        assertTrue(result is StoreResponse.Data)
    }

    @Test
    fun fetchWithIncorrectCustomHTTPError() = runTest {
        fetcher = mockFetcher.retry(RetryPolicy.ExponentialBackoff(maxRetries = 2, retryOnErrorCodes = listOf(500)))
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
        whenever(mockFetcher.fetch(any()))
            .thenReturn(customError())
            .thenReturn(customError())
            .thenReturn(successResult())
        val result = store.fetch(Key(id = 1))
        assertFalse(result is StoreResponse.Data)
    }

    @Test
    fun fetchWithCustomRetryPolicy() = runTest {
        fetcher = mockFetcher.retry(RetryPolicy.ExponentialBackoff(maxRetries = 2,
                retryOn = { error -> (error.error is FetcherError.HttpError && (error.error as FetcherError.HttpError).code == 503)})
            )
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
        whenever(mockFetcher.fetch(any()))
            .thenReturn(customError())
            .thenReturn(customError())
            .thenReturn(successResult())
        val result = store.fetch(Key(id = 1))
        assertTrue(result is StoreResponse.Data)
    }

    private fun successResult() = FetcherResult.Data(Entity(1, "One"))
    private fun serverError() = FetcherResult.Error(FetcherError.HttpError(Exception("Server Error"), 500, "Server Error"))
    private fun customError() = FetcherResult.Error(FetcherError.HttpError(Exception("Custom Server Error"), 503, "Custom Server Error"))

}