package com.pablodiste.android.datastore.impl

import com.pablodiste.android.datastore.*
import com.pablodiste.android.datastore.ratelimiter.RateLimiterFetcherController
import com.pablodiste.android.datastore.throttling.ThrottlingFetcherController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages a sequence of call identified by a Key.
 * Implements a rate limiter to avoid calling API multiple times.
 */
class FetcherController<K: Any, I: Any>(
    private val fetcher: Fetcher<K, I>
) {

    private val TAG = this.javaClass.simpleName

    private val rateLimiter = RateLimiterFetcherController.get<K>(
        fetcher.rateLimitPolicy.timeout,
        fetcher.rateLimitPolicy.timeUnit
    )
    private val throttlingController: ThrottlingFetcherController = StoreConfig.throttlingController

    /**
     * @param force when true makes the api call ignoring the rate limiter
     */
    suspend fun fetch(key: K, force: Boolean): FetcherResult<I> {
        val isLimiterDisabled: Boolean = StoreConfig.isRateLimiterEnabled().not()
        val isThrottlingDisabled: Boolean = StoreConfig.isThrottlingEnabled().not()

        return if (force || rateLimiter.shouldFetch(key) || isLimiterDisabled) {
            withContext(Dispatchers.IO) {
                if (!isThrottlingDisabled && throttlingController.isThrottling()) {
                    // Currently throttling requests
                    return@withContext FetcherResult.Error(throttlingController.throttlingError())
                } else {
                    try {
                        return@withContext fetcher.fetch(key)
                    } catch (e: Exception) {
                        if (throttlingController.isApiError(e)) throttlingController.onServerError()
                        rateLimiter.reset(key)
                        throw e
                    }
                }
            }
        } else {
            FetcherResult.NoData("No data")
        }
    }

}