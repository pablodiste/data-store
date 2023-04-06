package dev.pablodiste.datastore

enum class ResponseOrigin {
    SOURCE_OF_TRUTH,
    FETCHER
}

sealed class StoreResponse<out T> {

    fun requireData(): T {
        return when (this) {
            is Data -> value
            is Error -> throw error
            is NoData -> throw IllegalStateException("There is no data")
            is Loading -> throw IllegalStateException("Loading state, there is no data")
        }
    }

    fun requireOrigin(): ResponseOrigin {
        return when (this) {
            is Data -> origin
            is Error -> throw error
            is NoData -> throw IllegalStateException("There is no data")
            is Loading -> origin
        }
    }

    data class Data<T>(val value: T, val origin: ResponseOrigin): StoreResponse<T>()
    data class NoData(val message: String): StoreResponse<Nothing>()
    data class Error(val error: Throwable): StoreResponse<Nothing>()
    data class Loading(val origin: ResponseOrigin): StoreResponse<Nothing>()
}