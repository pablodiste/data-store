package com.pablodiste.android.datastore.impl

import com.pablodiste.android.datastore.CrudFetcher
import com.pablodiste.android.datastore.Fetcher
import com.pablodiste.android.datastore.ratelimiter.RateLimitPolicy
import java.util.concurrent.TimeUnit

abstract class LimitedFetcher<K: Any, I: Any>(
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
): Fetcher<K, I>

abstract class LimitedCrudFetcher<K: Any, I: Any>(
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
): CrudFetcher<K, I>

