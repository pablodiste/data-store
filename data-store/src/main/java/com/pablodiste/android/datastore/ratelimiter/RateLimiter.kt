package com.pablodiste.android.datastore.ratelimiter

import android.os.SystemClock
import com.pablodiste.android.datastore.FetcherResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.TimeUnit

/**
 * Decides whether we should fetch some data or not.
 */
class RateLimiter<I: Any>(timeout: Int, timeUnit: TimeUnit) {

    private val _onCompletionFlow = MutableSharedFlow<FetcherResult<I>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val onResultAvailable: Flow<FetcherResult<I>> = _onCompletionFlow.distinctUntilChanged()

    private var lastFetchedTime: Long? = null
    private var completed: Boolean = false
    private val timeout: Long = timeUnit.toMillis(timeout.toLong())

    val isCallInProgress: Boolean get() = !completed

    @Synchronized
    fun shouldFetch(): Boolean {
        val lastFetched = lastFetchedTime
        val now = nowTimestamp()
        if (lastFetched == null) {
            lastFetchedTime = now
            return true
        }
        if (now - lastFetched > timeout) {
            lastFetchedTime = now
            return true
        }
        return false
    }

    fun onResult(result: FetcherResult<I>) {
        _onCompletionFlow.tryEmit(result)
    }

    private fun nowTimestamp(): Long {
        return SystemClock.uptimeMillis()
    }

}
