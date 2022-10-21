package dev.pablodiste.datastore.retry

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed class RetryPolicy {
    data class ExponentialBackoff(
        val maxRetries: Int = 1,
        val initialBackoff: Duration = 2.seconds,
        val backoffRate: Int = 2): RetryPolicy()
    object DoNotRetry: RetryPolicy()
}
