package dev.pablodiste.datastore.impl

import android.util.Log
import dev.pablodiste.datastore.SourceOfTruth
import dev.pablodiste.datastore.ResponseOrigin
import dev.pablodiste.datastore.StoreResponse
import kotlinx.coroutines.flow.*

class PausableSourceOfTruth<K: Any, T: Any>(val sourceOfTruth: SourceOfTruth<K, T>): SourceOfTruth<K, T> {

    private var usingFetcher = false

    override fun listen(key: K): Flow<T> {
        return sourceOfTruth.listen(key)
            .onEach { Log.d("PausableSourceOfTruth", "Emitted") }
    }

    fun listenWithResponse(key: K): Flow<StoreResponse<T>> {
        return listen(key).map {
            if (usingFetcher) {
                usingFetcher = false
                StoreResponse.Data(it, ResponseOrigin.FETCHER)
            } else {
                StoreResponse.Data(it, ResponseOrigin.SOURCE_OF_TRUTH)
            }
        }
    }

    suspend fun storeAfterFetch(key: K, entity: T, removeStale: Boolean): T {
        usingFetcher = true
        val result = store(key, entity, removeStale)
        return result
    }

    override suspend fun store(key: K, entity: T, removeStale: Boolean): T = sourceOfTruth.store(key, entity, removeStale)

    override suspend fun exists(key: K): Boolean = sourceOfTruth.exists(key)

    override suspend fun get(key: K): T = sourceOfTruth.get(key)

    override suspend fun delete(key: K): Boolean = sourceOfTruth.delete(key)
}