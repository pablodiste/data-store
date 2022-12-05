package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class JoinInProgressCallsTest: CoroutineTest() {

    data class Key(val id: Int)
    data class Entity(val id: Int, val name: String)

    private lateinit var store: Store<Key, Entity>
    private lateinit var mockFetcher: Fetcher<Key, Entity>
    private lateinit var fetcher: Fetcher<Key, Entity>
    private lateinit var sourceOfTruth: SourceOfTruth<Key, Entity>

    @Before
    fun prepare() {
        mockFetcher = mock {
            onBlocking { fetch(any()) } doSuspendableAnswer {
                println("Fetching from network")
                delay(500)
                successResult()
            }
        }
        fetcher = mockFetcher.joinInProgressCalls()
        sourceOfTruth = object: InMemorySourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (value: Entity) -> Boolean = { value -> value.id == key.id }
        }
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
    }

    @Test
    fun normalFetch() = runTest(StandardTestDispatcher()) {
        println("First Request")
        val result1 = async { store.fetch(Key(id = 1)) }
        println("Second Request")
        val result2 = async { store.fetch(Key(id = 1)) }
        assertEquals(result1.await().requireData(), result2.await().requireData())
        verify(mockFetcher, times(1)).fetch(any())
    }

    private fun successResult() = FetcherResult.Data(Entity(1, "One"))
    private fun serverError() = FetcherResult.Error(FetcherError.HttpError(Exception("Server Error"), 500, "Server Error"))
    private fun customError() = FetcherResult.Error(FetcherError.HttpError(Exception("Custom Server Error"), 503, "Custom Server Error"))

}