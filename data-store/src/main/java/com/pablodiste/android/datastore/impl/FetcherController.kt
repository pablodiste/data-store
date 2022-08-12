package com.pablodiste.android.datastore.impl

import android.util.Log
import com.pablodiste.android.datastore.Fetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.StoreConfig
import com.pablodiste.android.datastore.ratelimiter.RateLimiter
import com.pablodiste.android.datastore.ratelimiter.RateLimiterFetcherController
import com.pablodiste.android.datastore.throttling.ThrottlingFetcherController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Manages a sequence of calls identified by a Key.
 * Implements a rate limiter to avoid calling API multiple times.
 */
class FetcherController<K: Any, I: Any>(
    private val fetcher: Fetcher<K, I>
) {

    private val TAG = this.javaClass.simpleName
    private val rateLimiterFetcherController: RateLimiterFetcherController get() = RateLimiterFetcherController

    private fun getRateLimiter(key: K): RateLimiter<I> {
        Log.d(TAG, "Limiting using key: $key")
        return rateLimiterFetcherController.get(key.toString(), fetcher.rateLimitPolicy.timeout, fetcher.rateLimitPolicy.timeUnit)
    }

    private val throttlingController: ThrottlingFetcherController = StoreConfig.throttlingController

    /**
     * @param force when true makes the api call ignoring the rate limiter
     */
    suspend fun fetch(key: K, force: Boolean): FetcherResult<I> {
        val isLimiterDisabled: Boolean = StoreConfig.isRateLimiterEnabled().not()
        val isThrottlingDisabled: Boolean = StoreConfig.isThrottlingEnabled().not()
        val rateLimiter = getRateLimiter(key)

        return if (force || isLimiterDisabled || rateLimiter.shouldFetch()) {
            if (isThrottlingDisabled || !throttlingController.isThrottling()) {
                try {
                    val response = withContext(Dispatchers.IO) {
                        return@withContext fetcher.fetch(key)
                    }
                    // We offer the result to any other duplicate request
                    rateLimiter.onResult(response)
                    response
                } catch (e: Exception) {
                    val response = FetcherResult.Error(e)
                    throttlingController.onException(e)
                    rateLimiter.onResult(response)
                    rateLimiterFetcherController.remove(key.toString())
                    response
                }
            } else {
                // Currently throttling requests
                FetcherResult.Error(throttlingController.throttlingError())
            }
        } else {
            if (rateLimiter.isCallInProgress) {
                Log.d(TAG, "Similar call is in progress, waiting for result")
                val result = rateLimiter.onResultAvailable.first()
                Log.d(TAG, "Result is available")
                // Marking it as non cacheable to avoid caching it multiple times
                if (result is FetcherResult.Data) result.copy(cacheable = false) else result
            } else {
                FetcherResult.NoData("Fetch not executed")
            }
        }
    }

}