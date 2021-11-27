package com.pablodiste.android.adapters.retrofit

import com.pablodiste.android.datastore.CrudFetcher
import com.pablodiste.android.datastore.FetcherResult
import com.pablodiste.android.datastore.impl.LimitedFetcher
import com.pablodiste.android.datastore.ratelimiter.RateLimitPolicy
import java.util.concurrent.TimeUnit

interface RetrofitServiceProvider {
    fun <T> createService(service: Class<T>): T
}

/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class RetrofitFetcher<K: Any, I: Any, S: Any>(
        serviceClass: Class<S>,
        serviceProvider: RetrofitServiceProvider,
        override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
    )
    : LimitedFetcher<K, I>(rateLimitPolicy) {

    protected val service = serviceProvider.createService(serviceClass)

    abstract suspend fun fetch(key: K, service: S): FetcherResult<I>

    override suspend fun fetch(key: K): FetcherResult<I> = fetch(key, service)

}

/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class RetrofitCrudFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: RetrofitServiceProvider,
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
): RetrofitFetcher<K, I, S>(serviceClass, serviceProvider, rateLimitPolicy), CrudFetcher<K, I> {

    abstract suspend fun create(entity: I, service: S): FetcherResult<I>
    abstract suspend fun update(entity: I, service: S): FetcherResult<I>
    abstract suspend fun delete(entity: I, service: S): Boolean

    override suspend fun fetch(key: K): FetcherResult<I> = fetch(key, service)
    override suspend fun create(entity: I): FetcherResult<I> = create(entity, service)
    override suspend fun update(entity: I): FetcherResult<I> = update(entity, service)
    override suspend fun delete(entity: I): Boolean = delete(entity, service)

}