package dev.pablodiste.datastore.fetchers

import android.util.Log
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.StoreConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
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
    private val loading: AtomicBoolean = AtomicBoolean(false)

    /**
     * @param force when true makes the api call ignoring the rate limiter. Force param does not ignore the throttling state.
     */
    suspend fun fetch(key: K, force: Boolean, emitLoadingState: Boolean): FetcherResult<I> {
        val config = StoreConfig.coroutineConfig
        return withContext(config.ioDispatcher + FetcherContextImpl(force)) {
            val mutableFetcherFlow = getMutableFetcherFlow(key)
            Log.d(TAG, "IsLoading: ${loading.get()}")
            if (emitLoadingState && loading.get().not()) mutableFetcherFlow.emit(FetcherResult.Loading)
            loading.set(true)
            val result = fetcher.fetch(key)
            mutableFetcherFlow.emit(result)
            loading.set(false)
            return@withContext result
        }
    }

    private fun getMutableFetcherFlow(key: K): MutableSharedFlow<FetcherResult<I>> {
        val fetcherFlow = keyBasedFlows[key.toString()] ?: MutableSharedFlow()
        keyBasedFlows[key.toString()] = fetcherFlow
        return fetcherFlow
    }

    fun getFetcherFlow(key: K): SharedFlow<FetcherResult<I>> = getMutableFetcherFlow(key)

    class FetcherContextImpl(override val forceFetch: Boolean) : FetcherContext
}

interface FetcherContext : CoroutineContext.Element {
    val forceFetch: Boolean
    override val key: CoroutineContext.Key<*> get() = Key
    companion object Key : CoroutineContext.Key<FetcherContext>
}