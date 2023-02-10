package dev.pablodiste.datastore.fetchers

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
