package dev.pablodiste.datastore

import app.cash.turbine.test
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
class StoreImplSingleResponseTest: CoroutineTest() {

    data class Key(val id: Int)
    data class Entity(val id: Int, val name: String)

    private lateinit var store: Store<Key, Entity>
    private lateinit var fetcher: Fetcher<Key, Entity>

    private val loading = StoreResponse.Loading(ResponseOrigin.FETCHER)
    private val entity1 = StoreResponse.Data(Entity(1, "One"), ResponseOrigin.FETCHER)
    private val entity2 = StoreResponse.Data(Entity(2, "Two"), ResponseOrigin.FETCHER)

    @Before
    fun prepare() {
        fetcher = mock<Fetcher<Key, Entity>> {
            onBlocking { fetch(Key(1)) } doReturn FetcherResult.Data(Entity(1, "One"))
            onBlocking { fetch(Key(2)) } doReturn FetcherResult.Data(Entity(2, "Two"))
        }
        val sourceOfTruth = object: InMemorySourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (value: Entity) -> Boolean = { value -> value.id == key.id }
        }
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
    }

    @Test
    fun testStreamEmissions() = runTest {
        assertEquals(entity1, store.get(Key(id = 1)))
        store.stream(key = Key(id = 1), refresh = false).test {

            val item = awaitItem()
            assertEquals(entity1, item) // This should be marked as FETCHER because get called the fetcher
            expectNoEvents()

            val fetched = store.fetch(Key(1))
            assertEquals(entity1, awaitItem())
            expectNoEvents()

            // When fetching an item with another id, the id = 1 is emitted because the SOT has been updated.
            val fetchedItem2 = store.fetch(Key(2))
            assertEquals(entity1.copy(origin = ResponseOrigin.SOURCE_OF_TRUTH), awaitItem())
            expectNoEvents()

        }
    }

    @Test
    fun testStreamEmissionsWithLoading() = runTest {
        store.stream(StoreRequest(key = Key(id = 1), refresh = true, emitLoadingStates = true)).test {
            assertEquals(loading, awaitItem())
            assertEquals(entity1, awaitItem())
            expectNoEvents()

            val fetched = store.fetch(StoreRequest(key = Key(id = 1), emitLoadingStates = true))
            assertEquals(loading, awaitItem())
            assertEquals(entity1, awaitItem())
            expectNoEvents()

            // When fetching an item with another id, the id = 1 is emitted because the SOT has been updated.
            val fetchedItem2 = store.fetch(Key(2))
            assertEquals(entity1.copy(origin = ResponseOrigin.SOURCE_OF_TRUTH), awaitItem())
            expectNoEvents()
        }
    }

}