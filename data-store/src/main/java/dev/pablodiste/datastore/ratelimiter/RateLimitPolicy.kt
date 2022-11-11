package dev.pablodiste.datastore.ratelimiter

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
sealed class RateLimitPolicy {
    data class FixedWindowPolicy(val duration: Duration, val eventCount: Int = 1, val timeSource: TimeSource = TimeSource.Monotonic): RateLimitPolicy()
    object FetchAlways: RateLimitPolicy()
    object FetchOnlyOnce: RateLimitPolicy()
}