package dev.pablodiste.datastore.fetchers

import android.util.Log
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.exceptions.FetcherError
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import java.util.*

/**
 * Fetcher that waits for previous repeated calls to complete before calling again.
 */
class JoinInProgressCallFetcher<K: Any, I: Any>(val fetcher: Fetcher<K, I>): Fetcher<K, I> {

    private val TAG = this.javaClass.simpleName
    private val keyBasedControllers: MutableMap<String, JoinInProgressCallController<I>> = Collections.synchronizedMap(mutableMapOf())

    override suspend fun fetch(key: K): FetcherResult<I> {
        val stringKey = key.toString()
        val controller = keyBasedControllers[stringKey] ?: JoinInProgressCallController<I>()
        if (!keyBasedControllers.containsKey(stringKey)) keyBasedControllers[stringKey] = controller

        return if (!controller.isCallInProgress) {
            controller.onRequest()
            val response = try {
                fetcher.fetch(key)
            } catch (e: Exception) {
                FetcherResult.Error(FetcherError.ClientError(e))
            }
            controller.onResult(response)
            response
        } else {
            Log.d(TAG, "Similar call is in progress, waiting for result")
            val result = controller.onResultAvailable.first()
            Log.d(TAG, "Result is available")
            // Marking it as non cacheable to avoid caching it multiple times
            if (result is FetcherResult.Data) result.copy(cacheable = false) else result
        }
    }

    class JoinInProgressCallController<I: Any> {

        private val _onCompletionFlow = MutableSharedFlow<FetcherResult<I>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        val onResultAvailable: Flow<FetcherResult<I>> = _onCompletionFlow.distinctUntilChanged()
        private var completed: Boolean = false
        val isCallInProgress: Boolean get() = !completed

        fun onRequest() {
            completed = false
        }

        suspend fun onResult(result: FetcherResult<I>) {
            _onCompletionFlow.emit(result)
            completed = true
        }
    }
}

fun <K: Any, I: Any> Fetcher<K, I>.joinInProgressCalls() = JoinInProgressCallFetcher(this)