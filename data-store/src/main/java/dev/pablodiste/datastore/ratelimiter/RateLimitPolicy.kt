package dev.pablodiste.datastore.ratelimiter

import java.util.concurrent.TimeUnit

open class RateLimitPolicy(val timeout: Int, val timeUnit: TimeUnit)

object FetchOnlyOnce: RateLimitPolicy(24, TimeUnit.HOURS)
object FetchAlways: RateLimitPolicy(0, TimeUnit.SECONDS)