package com.pablodiste.android.datastore.ratelimiter

import java.util.*
import java.util.concurrent.TimeUnit


object RateLimiterFetcherController {

    private val rateLimiters: MutableMap<String, RateLimiter<String>> = Collections.synchronizedMap(mutableMapOf())

    fun get(key: String, timeout: Int, timeUnit: TimeUnit): RateLimiter<String> {
        val rateLimiter = rateLimiters[key] ?: RateLimiter(timeout, timeUnit)
        add(key, rateLimiter)
        return rateLimiter
    }

    fun add(key: String, rateLimiter: RateLimiter<String>) {
        synchronized(this) {
            rateLimiters[key] = rateLimiter
        }
    }

    fun clear() {
        synchronized(this) {
            rateLimiters.values.forEach { it.clear() }
            rateLimiters.clear()
        }
    }

}