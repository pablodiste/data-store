package com.pablodiste.android.datastore.impl

import android.util.Log
import com.pablodiste.android.datastore.Fetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.StoreConfig
import com.pablodiste.android.datastore.ratelimiter.RateLimiter
import com.pablodiste.android.datastore.ratelimiter.RateLimiterFetcherController
import com.pablodiste.android.datastore.throttling.ThrottlingFetcherController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages a sequence of calls identified by a Key.
 * Implements a rate limiter to avoid calling API multiple times.
 */
class FetcherController<K: Any, I: Any>(
    private val fetcher: Fetcher<K, I>
) {

    private val TAG = this.javaClass.simpleName

    private fun getRateLimiter(key: K): RateLimiter<String> {
        Log.d(TAG, "Limiting using key: $key")
        return RateLimiterFetcherController.get(key.toString(), fetcher.rateLimitPolicy.timeout, fetcher.rateLimitPolicy.timeUnit)
    }

    private val throttlingController: ThrottlingFetcherController = StoreConfig.throttlingController

    /**
     * @param force when true makes the api call ignoring the rate limiter
     */
    suspend fun fetch(key: K, force: Boolean): FetcherResult<I> {
        val isLimiterDisabled: Boolean = StoreConfig.isRateLimiterEnabled().not()
        val isThrottlingDisabled: Boolean = StoreConfig.isThrottlingEnabled().not()
        val rateLimiter = getRateLimiter(key)

        return if (force || isLimiterDisabled || rateLimiter.shouldFetch(key.toString())) {
            if (isThrottlingDisabled || !throttlingController.isThrottling()) {
                try {
                    withContext(Dispatchers.IO) {
                        return@withContext fetcher.fetch(key)
                    }
                } catch (e: Exception) {
                    throttlingController.onException(e)
                    rateLimiter.reset(key.toString())
                    FetcherResult.Error(e)
                }
            } else {
                // Currently throttling requests
                FetcherResult.Error(throttlingController.throttlingError())
            }
        } else {
            FetcherResult.NoData("Fetch not executed")
        }
    }

}