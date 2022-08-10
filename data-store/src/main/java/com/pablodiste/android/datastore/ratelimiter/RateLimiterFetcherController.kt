package com.pablodiste.android.datastore.ratelimiter

import java.util.*
import java.util.concurrent.TimeUnit


object RateLimiterFetcherController {

    private val rateLimiters: MutableMap<String, RateLimiter<*>> = Collections.synchronizedMap(mutableMapOf())

     @Suppress("UNCHECKED_CAST")
     fun <I: Any> get(key: String, timeout: Int, timeUnit: TimeUnit): RateLimiter<I> {
        val rateLimiter = rateLimiters[key] ?: RateLimiter<I>(timeout, timeUnit)
        add(key, rateLimiter)
        return rateLimiter as RateLimiter<I>
    }

    fun remove(key: String) = rateLimiters.remove(key)

    fun clear() {
        synchronized(this) {
            rateLimiters.clear()
        }
    }

    private fun add(key: String, rateLimiter: RateLimiter<*>) {
        synchronized(this) {
            rateLimiters[key] = rateLimiter
        }
    }

}