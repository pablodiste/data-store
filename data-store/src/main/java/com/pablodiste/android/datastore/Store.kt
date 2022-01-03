package com.pablodiste.android.datastore

import com.pablodiste.android.datastore.ratelimiter.RateLimitPolicy
import kotlinx.coroutines.flow.Flow

interface Store<K: Any, T: Any> {
    fun stream(key: K, refresh: Boolean): Flow<StoreResponse<T>>
    suspend fun get(key: K): StoreResponse<T>
    suspend fun fetch(key: K, forced: Boolean = true): StoreResponse<T>
}

interface Cache<K: Any, T: Any> {
    suspend fun exists(key: K): Boolean
    fun listen(key: K): Flow<@JvmWildcard T>
    suspend fun store(key: K, entity: T, removeStale: Boolean = false): T
    suspend fun get(key: K): T
    suspend fun delete(key: K): Boolean
}

interface Fetcher<K: Any, I: Any> {
    val rateLimitPolicy: RateLimitPolicy
    suspend fun fetch(key: K): FetcherResult<I>
}