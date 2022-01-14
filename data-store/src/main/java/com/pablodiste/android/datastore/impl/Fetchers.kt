package com.pablodiste.android.datastore.impl

import com.pablodiste.android.datastore.CrudFetcher
import com.pablodiste.android.datastore.Fetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.ratelimiter.RateLimitPolicy
import java.util.concurrent.TimeUnit

abstract class LimitedFetcher<K: Any, I: Any>(
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)): Fetcher<K, I> {

    companion object {
        fun <K: Any, I: Any> of(
            fetch: suspend (K) -> FetcherResult<I>,
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
        ): Fetcher<K, I> {
            return object: LimitedFetcher<K, I>(rateLimitPolicy) {
                override suspend fun fetch(key: K): FetcherResult<I> = fetch(key)
            }
        }
    }
}

abstract class LimitedCrudFetcher<K: Any, I: Any>(
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
): CrudFetcher<K, I>

