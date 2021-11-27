package com.pablodiste.android.datastore.impl

import com.pablodiste.android.datastore.*
import kotlinx.coroutines.flow.Flow

data class NoKey(val value: Int = 0)

interface NoKeySimpleStore<T: Any>: Store<NoKey, T> {
    fun stream(refresh: Boolean): Flow<StoreResponse<T>>
    suspend fun get(): StoreResponse<T>
    suspend fun fetch(forced: Boolean = true): StoreResponse<T>
    suspend fun performFetch(forced: Boolean = false): StoreResponse<T>
}

open class NoKeySimpleStoreImpl<T: Any>(fetcher: Fetcher<NoKey, T>, cache: Cache<NoKey, T>)
    : SimpleStoreImpl<NoKey, T>(fetcher, cache), NoKeySimpleStore<T> {

    override fun stream(refresh: Boolean): Flow<StoreResponse<T>> {
        return super.stream(NoKey(), refresh)
    }

    override suspend fun get(): StoreResponse<T> {
        return super.get(NoKey())
    }

    override suspend fun fetch(forced: Boolean): StoreResponse<T> {
        return super.fetch(NoKey(), forced)
    }

    override suspend fun performFetch(forced: Boolean): StoreResponse<T> {
        return super.performFetch(NoKey(), forced)
    }

}