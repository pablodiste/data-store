package com.pablodiste.android.datastore.throttling

import kotlinx.coroutines.flow.MutableStateFlow
import java.net.UnknownHostException
import java.util.*

interface ThrottlingFetcherController {
    val throttlingObservable: MutableStateFlow<Long>
    fun isApiError(error: Throwable?): Boolean
    fun isThrottling(): Boolean
    fun throttlingError(): Exception
    fun onServerError()
    class ThrottlingError : Exception("Throttled Request Exception. Too many API errors.")
}

class ThrottlingFetcherControllerImpl: ThrottlingFetcherController {

    override val throttlingObservable: MutableStateFlow<Long> = MutableStateFlow(0L)
    private val serverErrorTimestamps: MutableList<Long> = mutableListOf()
    private var throttleBackoffFactor = 1

    override fun isThrottling() = Date().time - throttlingObservable.value < 0

    override fun throttlingError(): Exception = ThrottlingFetcherController.ThrottlingError()

    override fun isApiError(error: Throwable?): Boolean =
        error?.let { it is UnknownHostException /* || it is ApiErrorException || it is HttpException */ } ?: false

    /**
     * Record the timestamp of a server error. Use this to determine if we should
     * start throttling requests.
     * If the number of errors in the queue is larger than THROTTLE_ERROR_COUNT_THRESHOLD
     * and the the time between the first error and the current error is within
     * THROTTLE_ERROR_DURATION_THRESHOLD, set the time to throttle until.
     * If we were already throttling requests, exponentially increase this time .
     */
    override fun onServerError() {
        val now = Date().time
        synchronized(this) {
            serverErrorTimestamps.add(now)
            if (serverErrorTimestamps.size < THROTTLE_ERROR_COUNT_THRESHOLD) {
                // Not enough errors to start throttling
                throttlingObservable.tryEmit(0L)
                return
            }
            if (now - serverErrorTimestamps.removeAt(0) > THROTTLE_ERROR_DURATION_THRESHOLD) {
                // Error duration is low enough to not throttle
                throttlingObservable.tryEmit(0L)
                return
            }
        }
        startThrottlingRequests(now)
    }

    private fun startThrottlingRequests(nowTime: Long) {
        // If we were already throttling, increase the throttle time by a factor of 2
        throttleBackoffFactor = if (throttlingObservable.value!! == 0L) 1 else throttleBackoffFactor * 2
        var timeout: Long = throttleBackoffFactor * THROTTLE_INITIAL_TIMEOUT
        if (timeout > THROTTLE_ERROR_DURATION_THRESHOLD) timeout = THROTTLE_ERROR_DURATION_THRESHOLD
        throttlingObservable.tryEmit(nowTime + timeout)
    }

    companion object {
        private const val THROTTLE_ERROR_COUNT_THRESHOLD = 3
        private const val THROTTLE_ERROR_DURATION_THRESHOLD: Long = 60000 // msec
        private const val THROTTLE_INITIAL_TIMEOUT: Long = 15000 // msec
    }

}