package dev.pablodiste.datastore.ratelimiter

import kotlin.time.Duration

sealed class RateLimitPolicy {
    data class FixedWindowPolicy(val eventCount: Int = 1, val duration: Duration): RateLimitPolicy()
    object FetchAlways: RateLimitPolicy()
    object FetchOnlyOnce: RateLimitPolicy()
}