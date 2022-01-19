package com.pablodiste.android.datastore.impl

import com.pablodiste.android.datastore.*

open class CrudStoreImpl<K: Any, I: Any, T: Any>(
    fetcher: CrudFetcher<K, I>,
    cache: Cache<K, T>,
    mapper: Mapper<I, T>
):
    StoreImpl<K, I, T>(fetcher, cache, mapper), CrudStore<K, T> {

    protected val crudFetcher get() = fetcher as CrudFetcher

    override suspend fun create(key: K, entity: T): StoreResponse<T> {
        val createResult = crudFetcher.create(key, mapper.toFetcherEntity(entity))
        val storeResult = storeFetcherResult(createResult)
        return StoreResponse.Data(storeResult, ResponseOrigin.FETCHER)
    }

    override suspend fun update(key: K, entity: T): StoreResponse<T> {
        val updateResult = crudFetcher.update(key, mapper.toFetcherEntity(entity))
        val storeResult = storeFetcherResult(updateResult)
        return StoreResponse.Data(storeResult, ResponseOrigin.FETCHER)
    }

    override suspend fun delete(key: K, entity: T): Boolean {
        crudFetcher.delete(key, mapper.toFetcherEntity(entity))
        pausableCache.delete(key)
        return true
    }

    private suspend fun storeFetcherResult(result: FetcherResult<I>): T {
        return when (result) {
            is FetcherResult.Data -> {
                val entityToSave = mapper.toCacheEntity(result.value)
                pausableCache.store(buildKey(entityToSave), entityToSave)
            }
            is FetcherResult.Error -> throw result.error
            else -> throw Throwable("Error creating entity, invalid state")
        }
    }

    override fun buildKey(entity: T): K {
        TODO("Override this method providing a way to build the Key")
    }

}

/**
 * Simple CRUD store where the parsed fetcher entity type is the same as the cached entity type.
 */
open class SimpleCrudStoreImpl<K: Any, T: Any>(fetcher: CrudFetcher<K, T>, cache: Cache<K, T>):
    CrudStoreImpl<K, T, T>(fetcher, cache, SameEntityMapper())
