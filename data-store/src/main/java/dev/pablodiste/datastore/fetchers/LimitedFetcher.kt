package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.StoreConfig
import dev.pablodiste.datastore.ratelimiter.FetchAlwaysRateLimiter
import dev.pablodiste.datastore.ratelimiter.FetchOnlyOnceRateLimiter
import dev.pablodiste.datastore.ratelimiter.FixedWindowRateLimiter
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Limiter that avoid same-url calls to repeat.
 */
class LimitedFetcher<K: Any, I: Any>(
    val fetcher: Fetcher<K, I>,
    val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds)
): Fetcher<K, I> {

    private val TAG = this.javaClass.simpleName
    private val keyBasedControllers: MutableMap<String, RateLimiterController<I>> = Collections.synchronizedMap(mutableMapOf())

    override suspend fun fetch(key: K): FetcherResult<I> {
        val isLimiterDisabled: Boolean = StoreConfig.isRateLimiterEnabled().not()
        val stringKey = key.toString()
        val controller = keyBasedControllers[stringKey] ?: RateLimiterController(rateLimitPolicy)
        if (!keyBasedControllers.containsKey(stringKey)) keyBasedControllers[stringKey] = controller
        val rateLimiter = controller.rateLimiter
        val force = coroutineContext[FetcherContext]?.forceFetch ?: false

        return if (force || isLimiterDisabled || rateLimiter.shouldFetch()) {
            fetcher.fetch(key)
        } else {
            FetcherResult.NoData("Fetch was not executed. Call ignored by rate limiter.")
        }
    }

    @OptIn(ExperimentalTime::class)
    class RateLimiterController<I: Any>(val rateLimitPolicy: RateLimitPolicy) {
        val rateLimiter = when (rateLimitPolicy) {
            RateLimitPolicy.FetchAlways -> FetchAlwaysRateLimiter
            RateLimitPolicy.FetchOnlyOnce -> FetchOnlyOnceRateLimiter
            is RateLimitPolicy.FixedWindowPolicy -> FixedWindowRateLimiter(
                eventCount = rateLimitPolicy.eventCount, duration = rateLimitPolicy.duration, timeSource = rateLimitPolicy.timeSource
            )
        }
    }
}

fun <K: Any, I: Any> Fetcher<K, I>.limit(rateLimitPolicy: RateLimitPolicy) = LimitedFetcher(this, rateLimitPolicy)