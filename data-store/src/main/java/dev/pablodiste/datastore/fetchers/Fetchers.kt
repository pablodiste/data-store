package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import dev.pablodiste.datastore.retry.RetryPolicy
import kotlin.time.Duration.Companion.seconds

object FetcherBuilder {
    fun <K: Any, I: Any> of(
        rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds, eventCount = 1),
        retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry,
        fetch: suspend (K) -> FetcherResult<I>
    ): Fetcher<K, I> {
        return Fetcher<K, I> { key -> fetch(key) }
            .joinInProgressCalls()
            .limit(rateLimitPolicy)
            .retry(retryPolicy)
    }
}

abstract class LimitedCrudFetcher<K: Any, I: Any>: CrudFetcher<K, I> {

    companion object {
        fun <K: Any, I: Any> of(
            fetch: suspend (K) -> FetcherResult<I> = { FetcherResult.NoData("NOOP") },
            create: suspend (K, I) -> FetcherResult<I> = { _, _ -> FetcherResult.NoData("NOOP") },
            update: suspend (K, I) -> FetcherResult<I> = { _, _ -> FetcherResult.NoData("NOOP") },
            delete: suspend (K, I) -> FetcherResult<I> = { _, _ -> FetcherResult.NoData("NOOP") },
        ): CrudFetcher<K, I> {
            return object: LimitedCrudFetcher<K, I>() {
                override suspend fun fetch(key: K): FetcherResult<I> = fetch(key)
                override suspend fun create(key: K, entity: I): FetcherResult<I> = create(key, entity)
                override suspend fun update(key: K, entity: I): FetcherResult<I> = update(key, entity)
                override suspend fun delete(key: K, entity: I): FetcherResult<I> = delete(key, entity)
            }
        }
    }
}