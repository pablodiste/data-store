package dev.pablodiste.datastore.throttling

import dev.pablodiste.datastore.StoreConfig
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

interface ThrottlingFetcherController {
    val throttlingState: MutableStateFlow<ThrottlingState>
    fun isApiError(error: Exception?): Boolean
    fun isThrottling(): Boolean
    fun throttlingError(): Exception
    fun onException(error: Exception)
    fun onServerError()
    class ThrottlingError : Exception("Throttled Request Exception. Too many API errors.")
}

class ThrottlingFetcherControllerImpl: ThrottlingFetcherController {

    override val throttlingState: MutableStateFlow<ThrottlingState> = MutableStateFlow(
        ThrottlingState(isThrottling = false)
    )
    private val serverErrorTimestamps: MutableList<Long> = mutableListOf()
    private var throttleBackoffFactor = 1
    private val throttlingConfiguration: ThrottlingConfiguration get() = StoreConfig.throttlingConfiguration

    override fun isThrottling() = Date().time - throttlingState.value.timestampUntilNextCall < 0

    override fun throttlingError(): Exception = ThrottlingFetcherController.ThrottlingError()

    override fun onException(error: Exception) {
        if (isApiError(error)) onServerError()
    }

    override fun isApiError(error: Exception?): Boolean =
        error?.let {
            StoreConfig.throttlingDetectedExceptions.contains(error.javaClass)
        } ?: false
        // error?.let { it is IOException /* || it is ApiErrorException || it is HttpException */ } ?: false

    /**
     * Records the timestamp of a server error. Use this to determine if we should
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
            if (serverErrorTimestamps.size < throttlingConfiguration.errorCountThreshold) {
                // Not enough errors to start throttling
                throttlingState.tryEmit(ThrottlingState(isThrottling = false))
                return
            }
            if (now - serverErrorTimestamps.removeAt(0) > throttlingConfiguration.errorDurationThreshold) {
                // Error duration is low enough to not throttle
                throttlingState.tryEmit(ThrottlingState(isThrottling = false))
                return
            }
        }
        startThrottlingRequests(now)
    }

    private fun startThrottlingRequests(nowTime: Long) {
        // If we were already throttling, increase the throttle time by a factor of 2
        throttleBackoffFactor = if (throttlingState.value.timestampUntilNextCall == 0L) 1 else throttleBackoffFactor * 2
        var timeout: Long = throttleBackoffFactor * throttlingConfiguration.throttleInitialTimeout
        if (timeout > throttlingConfiguration.errorDurationThreshold) timeout = throttlingConfiguration.errorDurationThreshold
        throttlingState.tryEmit(ThrottlingState(isThrottling = true, timestampUntilNextCall = nowTime + timeout))
    }

}

data class ThrottlingState(val isThrottling: Boolean, val timestampUntilNextCall: Long = 0)

class ThrottlingConfiguration(
    val errorCountThreshold: Int = THROTTLE_ERROR_COUNT_THRESHOLD,
    val errorDurationThreshold: Long = THROTTLE_ERROR_DURATION_THRESHOLD,
    val throttleInitialTimeout: Long = THROTTLE_INITIAL_TIMEOUT
) {
    companion object {
        private const val THROTTLE_ERROR_COUNT_THRESHOLD = 3
        private const val THROTTLE_ERROR_DURATION_THRESHOLD: Long = 60000 // msec
        private const val THROTTLE_INITIAL_TIMEOUT: Long = 15000 // msec
    }
}