package com.pablodiste.android.datastore

enum class ResponseOrigin {
    CACHE,
    FETCHER
}

class StoreResponse<T>(val value: T, val origin: ResponseOrigin)