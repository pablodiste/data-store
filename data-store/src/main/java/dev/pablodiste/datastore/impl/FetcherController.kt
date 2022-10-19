package dev.pablodiste.datastore.impl

import android.util.Log
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.StoreConfig
import dev.pablodiste.datastore.ratelimiter.FetchAlwaysRateLimiter
import dev.pablodiste.datastore.ratelimiter.FetchOnlyOnceRateLimiter
import dev.pablodiste.datastore.ratelimiter.FixedWindowRateLimiter
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import dev.pablodiste.datastore.throttling.ThrottlingFetcherController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Manages a sequence of calls identified by a Key.
 * Implements a rate limiter to avoid calling API multiple times and handles throttling when receiving errors.
 */
class FetcherController<K: Any, I: Any>(
    private val fetcher: Fetcher<K, I>
) {

    private val TAG = this.javaClass.simpleName
    private val throttlingController: ThrottlingFetcherController = StoreConfig.throttlingController
    private val keyBasedControllers: MutableMap<String, KeyBasedController<I>> = Collections.synchronizedMap(mutableMapOf())

    /**
     * @param force when true makes the api call ignoring the rate limiter. Force param does not ignore the throttling state.
     */
    suspend fun fetch(key: K, force: Boolean): FetcherResult<I> {
        val isLimiterDisabled: Boolean = StoreConfig.isRateLimiterEnabled().not()
        val isThrottlingDisabled: Boolean = StoreConfig.isThrottlingEnabled().not()

        val stringKey = key.toString()
        val controller = keyBasedControllers[stringKey] ?: KeyBasedController(fetcher.rateLimitPolicy)
        if (!keyBasedControllers.containsKey(stringKey)) keyBasedControllers[stringKey] = controller

        val rateLimiter = controller.rateLimiter

        return if (force || isLimiterDisabled || rateLimiter.shouldFetch()) {
            if (isThrottlingDisabled || !throttlingController.isThrottling()) {
                try {
                    val response = withContext(Dispatchers.IO) {
                        controller.onRequest()
                        return@withContext fetcher.fetch(key)
                    }
                    // We offer the result to any other duplicate request
                    controller.onResult(response)
                    response
                } catch (e: Exception) {
                    val response = FetcherResult.Error(e)
                    throttlingController.onException(e)
                    controller.onResult(response)
                    response
                }
            } else {
                // Currently throttling requests
                FetcherResult.Error(throttlingController.throttlingError())
            }
        } else {
            if (controller.isCallInProgress) {
                Log.d(TAG, "Similar call is in progress, waiting for result")
                val result = controller.onResultAvailable.first()
                Log.d(TAG, "Result is available")
                // Marking it as non cacheable to avoid caching it multiple times
                if (result is FetcherResult.Data) result.copy(cacheable = false) else result
            } else {
                FetcherResult.NoData("Fetch not executed")
            }
        }
    }

    class KeyBasedController<I: Any>(val rateLimitPolicy: RateLimitPolicy) {

        private val _onCompletionFlow = MutableSharedFlow<FetcherResult<I>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        val onResultAvailable: Flow<FetcherResult<I>> = _onCompletionFlow.distinctUntilChanged()
        private var completed: Boolean = false
        val isCallInProgress: Boolean get() = !completed

        val rateLimiter = when (rateLimitPolicy) {
            RateLimitPolicy.FetchAlways -> FetchAlwaysRateLimiter
            RateLimitPolicy.FetchOnlyOnce -> FetchOnlyOnceRateLimiter
            is RateLimitPolicy.FixedWindowPolicy -> FixedWindowRateLimiter(eventCount = rateLimitPolicy.eventCount, duration = rateLimitPolicy.duration)
        }

        fun onRequest() {
            completed = false
        }

        suspend fun onResult(result: FetcherResult<I>) {
            _onCompletionFlow.emit(result)
            completed = true
        }
    }

}