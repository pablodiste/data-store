package com.pablodiste.android.datastore.ratelimiter

import java.util.*
import java.util.concurrent.TimeUnit


object RateLimiterFetcherController {

    private val limiters: MutableList<RateLimiter<*>> = Collections.synchronizedList(mutableListOf())

    fun <K: Any> get(timeout: Int, timeUnit: TimeUnit): RateLimiter<K> {
        val rateLimiter = RateLimiter<K>(timeout, timeUnit)
        add(rateLimiter)
        return rateLimiter
    }

    fun add(rateLimiter: RateLimiter<*>) {
        synchronized(this) {
            limiters.add(rateLimiter)
        }
    }

    fun clear() {
        synchronized(this) {
            limiters.forEach { it.clear() }
            limiters.clear()
        }
    }

}