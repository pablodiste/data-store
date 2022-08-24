package dev.pablodiste.datastore

import dev.pablodiste.datastore.impl.SimpleStoreImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class StoreTest {

    data class Key(val name: String = "")
    data class Entity(val name: String)

    @Test
    @ExperimentalCoroutinesApi
    fun storeGet() {
        val fetcher = mock<Fetcher<Key, Entity>>() {
            onBlocking { fetch(any()) } doReturn FetcherResult.Data(Entity("Test 1"))
        }
        val sourceOfTruth = mock<SourceOfTruth<Key, Entity>>() {
            onBlocking { exists(any()) } doReturn true
            onBlocking { get(any()) } doReturn Entity("Test 1")
        }

        runBlockingTest {
            val store = SimpleStoreImpl(fetcher, sourceOfTruth)
            val result = store.get(Key("1"))
            assertNotNull(result)
        }
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}