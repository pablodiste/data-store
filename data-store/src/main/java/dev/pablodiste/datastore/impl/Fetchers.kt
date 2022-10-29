package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import dev.pablodiste.datastore.retry.RetryPolicy
import kotlin.time.Duration.Companion.seconds

abstract class LimitedFetcher<K: Any, I: Any>(
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds),
    override val retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry
): Fetcher<K, I> {

    companion object {
        fun <K: Any, I: Any> of(
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds),
            retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry,
            fetch: suspend (K) -> FetcherResult<I>,
        ): Fetcher<K, I> {
            return object: LimitedFetcher<K, I>(rateLimitPolicy, retryPolicy) {
                override suspend fun fetch(key: K): FetcherResult<I> = fetch(key)
            }
        }
    }
}

abstract class LimitedCrudFetcher<K: Any, I: Any>(
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds),
    override val retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry
): CrudFetcher<K, I> {

    companion object {
        fun <K: Any, I: Any> of(
            fetch: suspend (K) -> FetcherResult<I> = { FetcherResult.NoData("NOOP") },
            create: suspend (K, I) -> FetcherResult<I> = { _, _ -> FetcherResult.NoData("NOOP") },
            update: suspend (K, I) -> FetcherResult<I> = { _, _ -> FetcherResult.NoData("NOOP") },
            delete: suspend (K, I) -> FetcherResult<I> = { _, _ -> FetcherResult.NoData("NOOP") },
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds),
            retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry
        ): CrudFetcher<K, I> {
            return object: LimitedCrudFetcher<K, I>(rateLimitPolicy, retryPolicy) {
                override suspend fun fetch(key: K): FetcherResult<I> = fetch(key)
                override suspend fun create(key: K, entity: I): FetcherResult<I> = create(key, entity)
                override suspend fun update(key: K, entity: I): FetcherResult<I> = update(key, entity)
                override suspend fun delete(key: K, entity: I): FetcherResult<I> = delete(key, entity)
            }
        }
    }
}
