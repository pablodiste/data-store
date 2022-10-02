package dev.pablodiste.datastore

import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import kotlinx.coroutines.flow.Flow

/**
 * An store is a repository implementation, in charge of fetching data from a remote source and allowing queries through a single source
 * of truth
 */
interface Store<K: Any, T: Any> {
    /**
     * Return a flow of data using the given key. The flow never completes.
     * @param key Key to identify the request
     * @param refresh When true it fetches data from the fetcher to update the source of truth
     */
    fun stream(request: StoreRequest<K>): Flow<StoreResponse<T>>

    /**
     * Returns data from the single source of truth using the given key.
     */
    suspend fun get(request: StoreRequest<K>): StoreResponse<T>

    /**
     * Tries to fetch data from the fetcher.
     * @param forced When true it always request new data from the fetcher.
     */
    suspend fun fetch(request: StoreRequest<K>): StoreResponse<T>
}

/**
 * Stores data locally
 */
interface SourceOfTruth<K: Any, T: Any> {
    /**
     * Return true when exists data in the source of truth with the provided key
     */
    suspend fun exists(key: K): Boolean

    /**
     * Returns a flow that never completes that queries for data with the given key and listen for updates in the source of truth
     */
    fun listen(key: K): Flow<@JvmWildcard T>

    /**
     * Stores the provided entity and returns the stored item
     * @param entity What we want to store
     * @param removeStale if true, it applies a staleness policy to delete entities before storing the new ones
     */
    suspend fun store(key: K, entity: T, removeStale: Boolean = false): T

    /**
     * Makes a query using the provided key and return the result immediately.
     */
    suspend fun get(key: K): T

    /**
     * Deletes the entities related to the provided key
     */
    suspend fun delete(key: K): Boolean
}

/**
 * Fetches data from a remote source, like an API
 */
interface Fetcher<K: Any, I: Any> {
    /**
     * Policy to limit the number of consecutive or concurrent calls
     */
    val rateLimitPolicy: RateLimitPolicy

    /**
     * Fetches data from the remote source.
     */
    suspend fun fetch(key: K): FetcherResult<I>
}

interface FetcherServiceProvider {
    fun <T> createService(service: Class<T>): T
}
