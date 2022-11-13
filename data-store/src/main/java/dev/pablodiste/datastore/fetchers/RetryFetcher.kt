package dev.pablodiste.datastore.fetchers

import android.util.Log
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.retry.DoNotRetry
import dev.pablodiste.datastore.retry.ExponentialBackoffRetry
import dev.pablodiste.datastore.retry.RetryPolicy
import kotlinx.coroutines.delay

class RetryFetcher<K: Any, I: Any>(val fetcher: Fetcher<K, I>, val retryPolicy: RetryPolicy): Fetcher<K, I> {

    private val TAG = this.javaClass.simpleName

    override suspend fun fetch(key: K): FetcherResult<I> {
        var response = fetcher.fetch(key)
        if (response is FetcherResult.Error) {
            response = retry(key, response)
        }
        return response
    }

    /**
     * After an error response we can retry the fetch automatically using a RetryPolicy.
     * At the moment the retries are not affected by the rate limiter or the throttling
     */
    private suspend fun retry(key: K, errorResponse: FetcherResult.Error): FetcherResult<I> {
        val retryMethod = when (val policy = retryPolicy) {
            RetryPolicy.DoNotRetry -> DoNotRetry()
            is RetryPolicy.ExponentialBackoff -> ExponentialBackoffRetry(
                policy.maxRetries, policy.initialBackoff, policy.backoffRate,
                policy.retryOnErrorCodes, policy.retryOn
            )
        }
        var lastError: FetcherResult.Error = errorResponse
        while (retryMethod.shouldRetry(lastError)) {
            val response = fetcher.fetch(key)
            if (response !is FetcherResult.Error) {
                return response
            } else {
                lastError = response
                Log.d(TAG, "Retrying in ${retryMethod.timeToNextRetry}")
                delay(retryMethod.timeToNextRetry)
            }
        }
        return lastError
    }
}

fun <K: Any, I: Any> Fetcher<K, I>.retry(retryPolicy: RetryPolicy) = RetryFetcher(this, retryPolicy)