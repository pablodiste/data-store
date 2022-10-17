package dev.pablodiste.datastore.ratelimiter

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Decides whether we should fetch some data or not.
 */
interface RateLimiter {
    fun shouldFetch(): Boolean
}


/**
 * Implements a fixed window algorithm for a rate limiter. It allows a max number of events in the window duration specified.
 * After the duration has passed you can start requesting again.
 */
@OptIn(ExperimentalTime::class)
class FixedWindowRateLimiter(
    private val eventCount: Int = 1,
    private val duration: Duration,
    private val timeSource: TimeSource = TimeSource.Monotonic): RateLimiter {

    private var requestCount: AtomicInteger = AtomicInteger(0)
    private var startMark: TimeMark? = null
    private var endMark: TimeMark? = null

    @Synchronized
    override fun shouldFetch(): Boolean {
        val start = startMark

        if (start == null) {
            startMark = timeSource.markNow().also { endMark = it + duration }
            requestCount.incrementAndGet()
            Log.d("RL", "Count: " + requestCount.get())
            return true
        }

        if (endMark?.hasPassedNow() == true) {
            requestCount.set(1)
            startMark = timeSource.markNow().also { endMark = it + duration }
            Log.d("RL", "Count: " + requestCount.get())
            return true
        }

        return (requestCount.incrementAndGet() <= eventCount).also {
            Log.d("RL", "Count: ${requestCount.get()} Max: $eventCount")
        }
    }

}

object FetchAlwaysRateLimiter: RateLimiter {
    override fun shouldFetch(): Boolean = true
}

object FetchOnlyOnceRateLimiter: RateLimiter {
    private val alreadyFetched = AtomicBoolean(false)
    override fun shouldFetch(): Boolean = alreadyFetched.getAndSet(true).not()
}