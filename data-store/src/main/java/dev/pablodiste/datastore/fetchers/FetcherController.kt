package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.StoreConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val keyBasedFlows: MutableMap<String, MutableSharedFlow<FetcherResult<I>>> = mutableMapOf()

    /**
     * @param force when true makes the api call ignoring the rate limiter. Force param does not ignore the throttling state.
     */
    suspend fun fetch(key: K, force: Boolean, emitLoadingState: Boolean): FetcherResult<I> {
        val config = StoreConfig.coroutineConfig
        return withContext(config.ioDispatcher + FetcherContextImpl(force)) {
            val mutableFetcherFlow = getMutableFetcherFlow(key)
            if (emitLoadingState) mutableFetcherFlow.emit(FetcherResult.Loading)
            val result = fetcher.fetch(key)
            mutableFetcherFlow.emit(result)
            return@withContext result
        }
    }

    private fun getMutableFetcherFlow(key: K): MutableSharedFlow<FetcherResult<I>> {
        val fetcherFlow = keyBasedFlows[key.toString()] ?: MutableSharedFlow()
        keyBasedFlows[key.toString()] = fetcherFlow
        return fetcherFlow
    }

    fun getFetcherFlow(key: K): SharedFlow<FetcherResult<I>> = getMutableFetcherFlow(key).asSharedFlow()

    class FetcherContextImpl(override val forceFetch: Boolean) : FetcherContext
}

interface FetcherContext : CoroutineContext.Element {
    val forceFetch: Boolean
    override val key: CoroutineContext.Key<*> get() = Key
    companion object Key : CoroutineContext.Key<FetcherContext>
}