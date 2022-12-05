@file:OptIn(ExperimentalTime::class)

package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.StoreConfig
import dev.pablodiste.datastore.exceptions.FetcherError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ThrottleOnErrorFetcher<K: Any, I: Any>(
    private val fetcher: Fetcher<K, I>,
    maxErrorCount: Int = 3,
    errorWindowDuration: Duration = 1.minutes,
    initialBackoff: Duration = 5.seconds,
    backoffRate: Int = 2,
    private val throttleOnErrorCodes: List<Int>? = null,
    private val throttleOn: ((FetcherResult.Error) -> Boolean)? = null,
    timeSource: TimeSource = TimeSource.Monotonic
): Fetcher<K, I> {

    private val TAG = this.javaClass.simpleName
    private val throttlingController: ThrottlingController =
        ThrottlingControllerImpl(maxErrorCount, errorWindowDuration, initialBackoff, backoffRate, timeSource)

    override suspend fun fetch(key: K): FetcherResult<I> {
        val isThrottlingDisabled: Boolean = StoreConfig.isThrottlingEnabled().not()
        val isThrottling = throttlingController.isThrottling
        return if (isThrottlingDisabled || !isThrottling) {
            try {
                val response = fetcher.fetch(key)
                if (response is FetcherResult.Error && isErrorDetected(response)) {
                    throttlingController.onError()
                } else if (response is FetcherResult.Data) {
                    throttlingController.onSuccess()
                }
                response
            } catch (e: Exception) {
                throttlingController.onError()
                FetcherResult.Error(FetcherError.ClientError(e))
            }
        } else {
            FetcherResult.Error(FetcherError.ClientError(ThrottlingError()))
        }
    }

    private fun isErrorDetected(response: FetcherResult.Error): Boolean = when {
        (throttleOn != null) -> throttleOn.invoke(response)
        (throttleOnErrorCodes != null) -> response.error is FetcherError.HttpError && response.error.code in throttleOnErrorCodes
        else -> response.error is FetcherError.HttpError
    }
}

interface ThrottlingController {
    val throttlingState: MutableStateFlow<ThrottlingState>
    val isThrottling: Boolean
    fun onError()
    fun onSuccess()
}

private class ThrottlingControllerImpl(
    private val maxErrorCount: Int = 3,
    private val errorWindowDuration: Duration = 1.minutes,
    private val initialBackoff: Duration = 5.seconds,
    private val backoffRate: Int = 2,
    private val timeSource: TimeSource = TimeSource.Monotonic
): ThrottlingController {

    override val throttlingState: MutableStateFlow<ThrottlingState> = MutableStateFlow(ThrottlingState(isThrottling = false))
    override val isThrottling: Boolean get() = throttlingState.value.isThrottling
    private val errorTimeMarks: MutableList<TimeMark> = mutableListOf()
    private var throttleBackoffFactor = 1

    override fun onError() {
        synchronized(this) {
            val lastestMark = timeSource.markNow()
            errorTimeMarks.add(lastestMark)
            if (errorTimeMarks.size < maxErrorCount) {
                // Not enough errors to start throttling
                throttlingState.value = ThrottlingState(isThrottling = false)
                return
            }
            val firstMark = errorTimeMarks.removeFirst()
            if (firstMark.elapsedNow() > errorWindowDuration) {
                // Error duration is low enough to not throttle
                throttlingState.value = ThrottlingState(isThrottling = false)
                return
            }
            // If we were already throttling, increase the throttle time by a factor of 2
            throttleBackoffFactor = if (isThrottling.not()) 1 else throttleBackoffFactor * backoffRate
            var timeout = throttleBackoffFactor * initialBackoff
            if (timeout > errorWindowDuration) timeout = errorWindowDuration
            throttlingState.value = ThrottlingState(isThrottling = true, timeMarkUntilCallsAreThrottled = lastestMark + timeout)
        }
    }

    override fun onSuccess() {
        synchronized(this) {
            val last = errorTimeMarks.lastOrNull()
            if (last != null && last.elapsedNow() > errorWindowDuration) {
                throttleBackoffFactor = 1
                errorTimeMarks.clear()
            }
        }
    }
}

data class ThrottlingState(val isThrottling: Boolean, val timeMarkUntilCallsAreThrottled: TimeMark? = null)

@OptIn(ExperimentalTime::class)
fun <K: Any, I: Any> Fetcher<K, I>.throttleOnError(
    maxErrorCount: Int = 3,
    errorWindowDuration: Duration = 1.minutes,
    initialBackoff: Duration = 5.seconds,
    backoffRate: Int = 2,
    throttleOnErrorCodes: List<Int>? = null,
    throttleOn: ((FetcherResult.Error) -> Boolean)? = null,
    timeSource: TimeSource = TimeSource.Monotonic
) = ThrottleOnErrorFetcher(this, maxErrorCount, errorWindowDuration, initialBackoff, backoffRate, throttleOnErrorCodes,
    throttleOn, timeSource)

class ThrottlingError : Exception("Throttled Request. Too many API errors.")
