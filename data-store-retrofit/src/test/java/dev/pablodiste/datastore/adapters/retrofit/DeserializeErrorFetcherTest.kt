package dev.pablodiste.datastore.adapters.retrofit

import com.google.gson.Gson
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.SourceOfTruth
import dev.pablodiste.datastore.Store
import dev.pablodiste.datastore.StoreResponse
import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.fetch
import dev.pablodiste.datastore.fetchers.FetcherException
import dev.pablodiste.datastore.impl.SimpleStoreImpl
import dev.pablodiste.datastore.inmemory.InMemorySourceOfTruth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@ExperimentalCoroutinesApi
class DeserializeErrorFetcherTest: CoroutineTest() {

    data class Key(val id: Int)
    data class Entity(val id: Int, val name: String)
    data class ErrorModel(val errorCode: Int = 1000)

    private lateinit var store: Store<Key, Entity>
    private lateinit var mockFetcher: Fetcher<Key, Entity>
    private lateinit var fetcher: Fetcher<Key, Entity>
    private lateinit var sourceOfTruth: SourceOfTruth<Key, Entity>
    private lateinit var retrofit: Retrofit

    @Before
    fun prepare() {
        retrofit = Retrofit.Builder()
            .baseUrl("https://example.com")
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .callFactory(OkHttpClient.Builder().build())
            .build()
        mockFetcher = mock {
            onBlocking { fetch(any()) } doSuspendableAnswer {
                println("Fetching from network")
                delay(500)
                customError()
            }
        }
        fetcher = mockFetcher.deserializeError<Key, Entity, ErrorModel>(retrofit)
        sourceOfTruth = object: InMemorySourceOfTruth<Key, Entity>() {
            override fun predicate(key: Key): (value: Entity) -> Boolean = { value -> value.id == key.id }
        }
        store = SimpleStoreImpl(fetcher, sourceOfTruth)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun fetchAndGetErrorModel() = runTest(StandardTestDispatcher()) {
        println("Request")
        val result = store.fetch(Key(id = 1))

        assertTrue(result is StoreResponse.Error)
        val fetcherError = ((result as StoreResponse.Error).error as FetcherException).fetcherError
        val parsedError = (fetcherError as FetcherError.EntityHttpError<ErrorModel>).errorResult
        assertEquals(1000, parsedError?.errorCode)
    }

    private fun customError() = FetcherResult.Error(
        FetcherError.HttpError(
            exception = HttpException(Response.error<Entity>(503, ResponseBody.create(null, "{ \"errorCode\":1000 }"))),
            code = 503, message = "Custom Server Error")
    )



}