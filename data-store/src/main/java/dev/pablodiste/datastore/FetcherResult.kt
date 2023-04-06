package dev.pablodiste.datastore

import dev.pablodiste.datastore.exceptions.FetcherError

sealed class FetcherResult<out I: Any> {
    object Loading : FetcherResult<Nothing>()
    data class Data<I: Any>(val value: I, val cacheable: Boolean = true): FetcherResult<I>()
    data class Success(val success: Boolean): FetcherResult<Nothing>()
    data class NoData(val message: String): FetcherResult<Nothing>()
    data class Error(val error: FetcherError): FetcherResult<Nothing>()
}
