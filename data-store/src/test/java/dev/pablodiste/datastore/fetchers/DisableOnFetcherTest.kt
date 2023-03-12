package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.CoroutineTest
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.SourceOfTruth
import dev.pablodiste.datastore.Store
import dev.pablodiste.datastore.StoreResponse
import dev.pablodiste.datastore.fetch
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class DisableOnFetcherTest: CoroutineTest() {

    data class Key(val id: Int)
    data class Entity(val id: Int, val name: String)

    private lateinit var store: Store<Key, Entity>
    private lateinit var mockFetcher: Fetcher<Key, Entity>
    private lateinit var fetcher: Fetcher<Key, Entity>
    private lateinit var sourceOfTruth: SourceOfTruth<Key, Entity>

    private var disabled = false

    @Before
    fun prepare() {
        mockFetcher = mock()
        fetcher = mockFetcher.disableOn { disabled }
        sourceOfTruth = object: InMemorySourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (value: Entity) -> Boolean = { value -> value.id == key.id }
        }
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
    }

    @Test
    fun fetcherDisabled() = runTest {
        whenever(mockFetcher.fetch(any())) doReturn successResult()
        disabled = true
        val result = store.fetch(Key(id = 1))
        assertNotNull(result)
        assertTrue(result is StoreResponse.NoData)
    }

    @Test
    fun fetcherDisabledAndReEnabled() = runTest {
        whenever(mockFetcher.fetch(any())) doReturn successResult()
        disabled = true
        val result = store.fetch(Key(id = 1))
        assertNotNull(result)
        assertTrue(result is StoreResponse.NoData)
        disabled = false
        val result2 = store.fetch(Key(id = 1))
        assertNotNull(result2)
        assertTrue(result2 is StoreResponse.Data)
    }

    private fun successResult() = FetcherResult.Data(Entity(1, "One"))
}