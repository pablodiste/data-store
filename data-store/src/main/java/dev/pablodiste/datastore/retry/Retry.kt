package dev.pablodiste.datastore.retry

import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.exceptions.FetcherError
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

interface Retry {
    val timeToNextRetry: Duration
    fun shouldRetry(error: FetcherResult.Error): Boolean
}

class ExponentialBackoffRetry(private val maxRetries: Int = 1,
                              private val initialBackoff: Duration = 2.seconds,
                              private val backoffRate: Int = 2,
                              private val retryOnErrorCodes: List<Int>? = null,
                              private val retryOn: ((FetcherResult.Error) -> Boolean)? = null
): Retry {

    private var retryCount = 0
    private var timeToRetry = initialBackoff
    override val timeToNextRetry: Duration get() = timeToRetry

    override fun shouldRetry(error: FetcherResult.Error): Boolean {
        return when {
            (retryOnErrorCodes != null) ->
                if (error.error is FetcherError.HttpError && retryOnErrorCodes.contains(error.error.code)) increaseRetryCount() else false
            (retryOn != null) -> if (retryOn.invoke(error)) increaseRetryCount() else false
            else -> increaseRetryCount()
        }
    }

    private fun increaseRetryCount(): Boolean {
        if (retryCount > 0) {
            timeToRetry *= backoffRate
        }
        return ++retryCount <= maxRetries
    }
}

class DoNotRetry: Retry {
    override val timeToNextRetry: Duration = ZERO
    override fun shouldRetry(error: FetcherResult.Error): Boolean = false
}