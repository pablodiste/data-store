package com.pablodiste.android.datastore

sealed class FetcherResult<out I : Any> {
    data class Data<I : Any>(val value: I): FetcherResult<I>()
    data class NoData(val message: String): FetcherResult<Nothing>()
    data class Error(val error: Throwable): FetcherResult<Nothing>()
}