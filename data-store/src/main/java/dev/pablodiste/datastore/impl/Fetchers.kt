package dev.pablodiste.datastore.impl

import android.util.Log
import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.StoreConfig
import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.ratelimiter.FetchAlwaysRateLimiter
import dev.pablodiste.datastore.ratelimiter.FetchOnlyOnceRateLimiter
import dev.pablodiste.datastore.ratelimiter.FixedWindowRateLimiter
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import dev.pablodiste.datastore.retry.DoNotRetry
import dev.pablodiste.datastore.retry.ExponentialBackoffRetry
import dev.pablodiste.datastore.retry.RetryPolicy
import dev.pablodiste.datastore.throttling.ThrottlingFetcherController
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.seconds

object FetcherBuilder {
    fun <K: Any, I: Any> of(fetch: suspend (K) -> FetcherResult<I>): Fetcher<K, I> = Fetcher { key -> fetch(key) }
}


/**
 * Fetcher that waits for previous repeated calls to complete before calling again.
 */
class JoinInProgressCallFetcher<K: Any, I: Any>(val fetcher: Fetcher<K, I>): Fetcher<K, I>{

    private val TAG = this.javaClass.simpleName
    private val keyBasedControllers: MutableMap<String, JoinInProgressCallController<I>> = Collections.synchronizedMap(mutableMapOf())

    override suspend fun fetch(key: K): FetcherResult<I> {
        val stringKey = key.toString()
        val controller = keyBasedControllers[stringKey] ?: JoinInProgressCallController<I>()
        if (!keyBasedControllers.containsKey(stringKey)) keyBasedControllers[stringKey] = controller

        return if (!controller.isCallInProgress) {
            controller.onRequest()
            val response = try {
                fetcher.fetch(key)
            } catch (e: Exception) {
                FetcherResult.Error(FetcherError.ClientError(e))
            }
            controller.onResult(response)
            response
        } else {
            Log.d(TAG, "Similar call is in progress, waiting for result")
            val result = controller.onResultAvailable.first()
            Log.d(TAG, "Result is available")
            // Marking it as non cacheable to avoid caching it multiple times
            if (result is FetcherResult.Data) result.copy(cacheable = false) else result
        }
    }

    class JoinInProgressCallController<I: Any> {

        private val _onCompletionFlow = MutableSharedFlow<FetcherResult<I>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        val onResultAvailable: Flow<FetcherResult<I>> = _onCompletionFlow.distinctUntilChanged()
        private var completed: Boolean = false
        val isCallInProgress: Boolean get() = !completed

        fun onRequest() {
            completed = false
        }

        suspend fun onResult(result: FetcherResult<I>) {
            _onCompletionFlow.emit(result)
            completed = true
        }
    }
}

fun <K: Any, I: Any> Fetcher<K, I>.joinInProgressCalls() = JoinInProgressCallFetcher(this)


/**
 * Limiter that avoid same-url calls to repeat.
 */
class LimitedFetcher<K: Any, I: Any>(
    val fetcher: Fetcher<K, I>,
    val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds)): Fetcher<K, I>{

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
            FetcherResult.NoData("Fetch not executed")
        }
    }

    class RateLimiterController<I: Any>(val rateLimitPolicy: RateLimitPolicy) {
        val rateLimiter = when (rateLimitPolicy) {
            RateLimitPolicy.FetchAlways -> FetchAlwaysRateLimiter
            RateLimitPolicy.FetchOnlyOnce -> FetchOnlyOnceRateLimiter
            is RateLimitPolicy.FixedWindowPolicy -> FixedWindowRateLimiter(eventCount = rateLimitPolicy.eventCount, duration = rateLimitPolicy.duration)
        }
    }
}
fun <K: Any, I: Any> Fetcher<K, I>.limit(rateLimitPolicy: RateLimitPolicy) = LimitedFetcher(this, rateLimitPolicy)

class ThrottleOnErrorFetcher<K: Any, I: Any>(val fetcher: Fetcher<K, I>): Fetcher<K, I>{

    private val TAG = this.javaClass.simpleName
    private val throttlingController: ThrottlingFetcherController = StoreConfig.throttlingController

    override suspend fun fetch(key: K): FetcherResult<I> {
        val isThrottlingDisabled: Boolean = StoreConfig.isThrottlingEnabled().not()
        return if (isThrottlingDisabled || !throttlingController.isThrottling()) {
            try {
                val response = fetcher.fetch(key)
                if (response is FetcherResult.Error) {
                    throttlingController.onException(response.error.exception)
                }
                response
            } catch (e: Exception) {
                throttlingController.onException(e)
                FetcherResult.Error(FetcherError.ClientError(e))
            }
        } else {
            FetcherResult.Error(FetcherError.ClientError(throttlingController.throttlingError()))
        }
    }
}

fun <K: Any, I: Any> Fetcher<K, I>.throttleOnError() = ThrottleOnErrorFetcher(this)

class RetryFetcher<K: Any, I: Any>(val fetcher: Fetcher<K, I>, val retryPolicy: RetryPolicy): Fetcher<K, I>{

    private val TAG = this.javaClass.simpleName

    override suspend fun fetch(key: K): FetcherResult<I> {
        var response = fetcher.fetch(key)
        if (response is FetcherResult.Error) {
            response = retry(key, response)
        }
        return response
    }

    /**
     * After an error response we can retry the fetch automatically using a RetryPolicy.
     * At the moment the retries are not affected by the rate limiter or the throttling
     */
    private suspend fun retry(key: K, errorResponse: FetcherResult.Error): FetcherResult<I> {
        val retryMethod = when (val policy = retryPolicy) {
            RetryPolicy.DoNotRetry -> DoNotRetry()
            is RetryPolicy.ExponentialBackoff -> ExponentialBackoffRetry(policy.maxRetries, policy.initialBackoff, policy.backoffRate,
                policy.retryOnErrorCodes, policy.retryOn)
        }
        var lastError: FetcherResult.Error = errorResponse
        while (retryMethod.shouldRetry(lastError)) {
            val response = fetcher.fetch(key)
            if (response !is FetcherResult.Error) {
                return response
            } else {
                lastError = response
                Log.d(TAG, "Retrying in ${retryMethod.timeToNextRetry}")
                delay(retryMethod.timeToNextRetry)
            }
        }
        return lastError
    }
}

fun <K: Any, I: Any> Fetcher<K, I>.retry(retryPolicy: RetryPolicy) = RetryFetcher(this, retryPolicy)

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