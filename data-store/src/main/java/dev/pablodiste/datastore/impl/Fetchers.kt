package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import java.util.concurrent.TimeUnit

abstract class LimitedFetcher<K: Any, I: Any>(
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)): Fetcher<K, I> {

    companion object {
        fun <K: Any, I: Any> of(
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS),
            fetch: suspend (K) -> FetcherResult<I>,
        ): Fetcher<K, I> {
            return object: LimitedFetcher<K, I>(rateLimitPolicy) {
                override suspend fun fetch(key: K): FetcherResult<I> = fetch(key)
            }
        }
    }
}

abstract class LimitedCrudFetcher<K: Any, I: Any>(
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
): CrudFetcher<K, I> {

    companion object {
        fun <K: Any, I: Any> of(
            fetch: suspend (K) -> FetcherResult<I> = { FetcherResult.NoData("NOOP") },
            create: suspend (K, I) -> FetcherResult<I> = { _, _ -> FetcherResult.NoData("NOOP") },
            update: suspend (K, I) -> FetcherResult<I> = { _, _ -> FetcherResult.NoData("NOOP") },
            delete: suspend (K, I) -> Boolean = { _, _ -> false },
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
        ): CrudFetcher<K, I> {
            return object: LimitedCrudFetcher<K, I>(rateLimitPolicy) {
                override suspend fun fetch(key: K): FetcherResult<I> = fetch(key)
                override suspend fun create(key: K, entity: I): FetcherResult<I> = create(key, entity)
                override suspend fun update(key: K, entity: I): FetcherResult<I> = update(key, entity)
                override suspend fun delete(key: K, entity: I): Boolean = delete(key, entity)
            }
        }
    }
}

