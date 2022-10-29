package dev.pablodiste.datastore.retry

import dev.pablodiste.datastore.FetcherResult
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed class RetryPolicy {
    data class ExponentialBackoff(
        val maxRetries: Int = 1,
        val initialBackoff: Duration = 2.seconds,
        val backoffRate: Int = 2,
        val retryOnErrorCodes: List<Int>? = null,
        val retryOn: ((FetcherResult.Error) -> Boolean)? = null
    ): RetryPolicy()
    object DoNotRetry: RetryPolicy()
}
