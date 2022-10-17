package dev.pablodiste.datastore

import dev.pablodiste.datastore.ratelimiter.FixedWindowRateLimiter
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource

@OptIn(ExperimentalCoroutinesApi::class)
class RateLimiterTest: CoroutineTest() {

    @Test
    @OptIn(ExperimentalTime::class)
    fun testFixedWindowCountLimit() = runTest {
        val timeSource = TestTimeSource()
        val rateLimiter = FixedWindowRateLimiter(eventCount = 2, duration = 1.seconds, timeSource = timeSource)
        assertTrue(rateLimiter.shouldFetch())
        assertTrue(rateLimiter.shouldFetch())
        assertFalse(rateLimiter.shouldFetch())
        timeSource.plusAssign(3.seconds)
        assertTrue(rateLimiter.shouldFetch())
    }

    @Test
    @OptIn(ExperimentalTime::class)
    fun testFixedWindowDuration() = runTest {
        val timeSource = TestTimeSource()
        val rateLimiter = FixedWindowRateLimiter(eventCount = 3, duration = 1.seconds, timeSource = timeSource)
        assertTrue(rateLimiter.shouldFetch())
        timeSource.plusAssign(3.seconds)
        assertTrue(rateLimiter.shouldFetch())
        assertTrue(rateLimiter.shouldFetch())
        assertTrue(rateLimiter.shouldFetch())
        assertFalse(rateLimiter.shouldFetch())
        timeSource.plusAssign(2.seconds)
        assertTrue(rateLimiter.shouldFetch())
    }

}