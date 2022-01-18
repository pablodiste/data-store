package com.pablodiste.android.datastore.ratelimiter

import android.os.SystemClock
import android.util.ArrayMap
import java.util.concurrent.TimeUnit

/**
 * Utility class that decides whether we should fetch some data or not.
 * Taken from android-architecture-components
 */
class RateLimiter<K>(timeout: Int, timeUnit: TimeUnit) {

    private val timestamps = ArrayMap<K, Long>()
    private val timeout: Long = timeUnit.toMillis(timeout.toLong())

    @Synchronized
    fun shouldFetch(key: K): Boolean {
        val lastFetched = timestamps[key]
        val now = nowTimestamp()
        if (lastFetched == null) {
            timestamps[key] = now
            return true
        }
        if (now - lastFetched > timeout) {
            timestamps[key] = now
            return true
        }
        return false
    }

    private fun nowTimestamp(): Long {
        return SystemClock.uptimeMillis()
    }

    @Synchronized
    fun reset(key: K) {
        timestamps.remove(key)
    }

    @Synchronized
    fun clear() {
        timestamps.clear()
    }

}

object FetchOnlyOnce: RateLimitPolicy(24, TimeUnit.HOURS)
object FetchAlways: RateLimitPolicy(0, TimeUnit.SECONDS)