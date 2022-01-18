package com.pablodiste.android.datastore

enum class ResponseOrigin {
    CACHE,
    FETCHER
}

sealed class StoreResponse<out T> {

    fun requireData(): T {
        return when (this) {
            is Data -> value
            is Error -> throw error
            is NoData -> throw IllegalStateException("There is no data")
        }
    }

    fun requireOrigin(): ResponseOrigin {
        return when (this) {
            is Data -> origin
            is Error -> throw error
            is NoData -> throw IllegalStateException("There is no data")
        }
    }

    data class Data<T>(val value: T, val origin: ResponseOrigin): StoreResponse<T>()
    data class NoData(val message: String): StoreResponse<Nothing>()
    data class Error(val error: Throwable): StoreResponse<Nothing>()
}