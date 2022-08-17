package dev.pablodiste.datastore

sealed class FetcherResult<out I : Any> {
    data class Data<I : Any>(val value: I, val cacheable: Boolean = true): FetcherResult<I>()
    data class NoData(val message: String): FetcherResult<Nothing>()
    data class Error(val error: Throwable): FetcherResult<Nothing>()
}