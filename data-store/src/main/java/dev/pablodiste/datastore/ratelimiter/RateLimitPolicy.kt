package dev.pablodiste.datastore.ratelimiter

import kotlin.time.Duration

sealed class RateLimitPolicy {
    data class FixedWindowPolicy(val duration: Duration, val eventCount: Int = 1): RateLimitPolicy()
    object FetchAlways: RateLimitPolicy()
    object FetchOnlyOnce: RateLimitPolicy()
}