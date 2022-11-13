package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.StoreConfig
import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.throttling.ThrottlingFetcherController

class ThrottleAllStoresOnErrorFetcher<K: Any, I: Any>(val fetcher: Fetcher<K, I>): Fetcher<K, I> {

    private val TAG = this.javaClass.simpleName
    private val throttlingController: ThrottlingFetcherController = StoreConfig.throttlingController

    override suspend fun fetch(key: K): FetcherResult<I> {
        val isThrottlingDisabled: Boolean = StoreConfig.isThrottlingEnabled().not()
        return if (isThrottlingDisabled || !throttlingController.isThrottling()) {
            try {
                val response = fetcher.fetch(key)
                if (response is FetcherResult.Error) {
                    throttlingController.onException(response.error.exception)
                }
                response
            } catch (e: Exception) {
                throttlingController.onException(e)
                FetcherResult.Error(FetcherError.ClientError(e))
            }
        } else {
            FetcherResult.Error(FetcherError.ClientError(throttlingController.throttlingError()))
        }
    }
}

fun <K: Any, I: Any> Fetcher<K, I>.throttleAllStoresOnError() = ThrottleAllStoresOnErrorFetcher(this)