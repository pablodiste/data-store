package dev.pablodiste.datastore

import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy

/**
 * Sends data to a remote source, like an API
 */
fun interface Sender<K: Any, I: Any> {
    /**
     * Sends data to the remote source.
     */
    suspend fun send(key: K, entity: I, changeOperation: ChangeOperation): FetcherResult<I>
}

interface WritableStore<K: Any, T: Any>: Store<K, T> {
    suspend fun put(key: K, entity: T, change: EntityChange<T>, operation: ChangeOperation): StoreResponse<T>
    suspend fun remove(key: K, entity: T): StoreResponse<Boolean>
}

typealias EntityChange<T> = (T) -> T

enum class ChangeOperation {
    CREATE, UPDATE, DELETE
}
