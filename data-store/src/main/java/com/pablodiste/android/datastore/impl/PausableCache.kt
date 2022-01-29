package com.pablodiste.android.datastore.impl

import android.util.Log
import com.pablodiste.android.datastore.Cache
import com.pablodiste.android.datastore.ResponseOrigin
import com.pablodiste.android.datastore.StoreResponse
import kotlinx.coroutines.flow.*

class PausableCache<K: Any, T: Any>(val cache: Cache<K, T>): Cache<K, T> {

    private var paused = false
    private var resumed = false

    override fun listen(key: K): Flow<T> {
        return cache.listen(key)
            .onEach { Log.d("PausableCache", "Emitio") }
    }

    fun listenWithResponse(key: K): Flow<StoreResponse<T>> {
        return listen(key).map {
            if (resumed) {
                resumed = false
                StoreResponse.Data(it, ResponseOrigin.FETCHER)
            } else {
                StoreResponse.Data(it, ResponseOrigin.CACHE)
            }
        }
    }

    override suspend fun store(key: K, entity: T, removeStale: Boolean): T {
        paused = true
        val result = cache.store(key, entity, removeStale)
        paused = false
        resumed = true
        return result
    }

    override suspend fun exists(key: K): Boolean = cache.exists(key)

    override suspend fun get(key: K): T = cache.get(key)

    override suspend fun delete(key: K): Boolean = cache.delete(key)
}