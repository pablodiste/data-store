package dev.pablodiste.datastore

import app.cash.turbine.test
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.inmemory.InMemoryListSourceOfTruth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
class StoreImplTest: CoroutineTest() {

    data class Key(val cid: Int)
    data class Entity(val id: Int, val cid: Int, val name: String)

    private lateinit var store: Store<Key, List<Entity>>
    private lateinit var fetcher: Fetcher<Key, List<Entity>>

    @Before
    fun prepare() {
        fetcher = mock {
            onBlocking { fetch(Key(1)) } doReturn FetcherResult.Data(listOf(
                Entity(1, 1, "One - First"),
                Entity(2, 1, "One - Second"),
                Entity(3, 1, "One - Third")
            ))
            onBlocking { fetch(Key(2)) } doReturn FetcherResult.Data(listOf(
                Entity(1, 2, "Two - First"),
                Entity(2, 2, "Two - Second"),
                Entity(3, 2, "Two - Third"),
                Entity(4, 2, "Two - Fourth")
            ))
        }
        val sourceOfTruth = object: InMemoryListSourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (key: Key, value: Entity) -> Boolean = { key, value -> value.cid == key.cid }
        }
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
    }

    @Test
    fun storeGet() = runTest {
        val result1 = store.get(Key(1))
        assertEquals(3, result1.requireData().size)
        val result2 = store.get(Key(2))
        assertEquals(4, result2.requireData().size)
    }

    @Test
    fun storeGetAndNotFound() = runTest {
        val result = store.get(StoreRequest(Key(1), fetchWhenNoDataFound = false))
        assert(result is StoreResponse.Error)
    }

    @Test
    fun storeStream() = runTest {
        // Stream for non existing item should call the fetcher
        store.stream(Key(1), refresh = true).test {
            val data = awaitItem() as StoreResponse.Data
            assertEquals(ResponseOrigin.FETCHER, data.origin)
            cancelAndIgnoreRemainingEvents()
        }
        // Further streams should return cached data
        store.stream(Key(1), refresh = false).test {
            val data = awaitItem() as StoreResponse.Data
            assertEquals(ResponseOrigin.SOURCE_OF_TRUTH, data.origin)
            cancelAndIgnoreRemainingEvents()
        }
        // With refresh true it should return SOT item first and then call the fetcher to refresh the data.
        store.stream(Key(1), refresh = true).test {
            val dataSOT = awaitItem() as StoreResponse.Data
            assertEquals(ResponseOrigin.SOURCE_OF_TRUTH, dataSOT.origin)
            val dataFetcher = awaitItem() as StoreResponse.Data
            assertEquals(ResponseOrigin.FETCHER, dataFetcher.origin)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun storeFetch() = runTest {
        val result1 = store.fetch(Key(1))
        assertEquals(3, result1.requireData().size)
        val result2 = store.get(Key(1))
        assertEquals(3, result2.requireData().size)
    }

}