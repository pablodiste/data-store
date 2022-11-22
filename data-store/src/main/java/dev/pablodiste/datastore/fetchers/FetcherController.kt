package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.StoreConfig
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Manages a sequence of calls identified by a Key.
 * Implements a rate limiter to avoid calling API multiple times and handles throttling when receiving errors.
 */
class FetcherController<K: Any, I: Any>(
    private val fetcher: Fetcher<K, I>
) {
    private val TAG = this.javaClass.simpleName

    /**
     * @param force when true makes the api call ignoring the rate limiter. Force param does not ignore the throttling state.
     */
    suspend fun fetch(key: K, force: Boolean): FetcherResult<I> {
        val config = StoreConfig.coroutineConfig
        return withContext(config.ioDispatcher + FetcherContextImpl(force)) {
            return@withContext fetcher.fetch(key)
        }
    }

    class FetcherContextImpl(override val forceFetch: Boolean) : FetcherContext
}

interface FetcherContext : CoroutineContext.Element {
    val forceFetch: Boolean
    override val key: CoroutineContext.Key<*> get() = Key
    companion object Key : CoroutineContext.Key<FetcherContext>
}