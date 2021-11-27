package com.pablodiste.android.datastore.impl

import com.pablodiste.android.datastore.*

abstract class CrudStoreImpl<K: Any, I: Any, T: Any>(
    fetcher: CrudFetcher<K, I>,
    cache: Cache<K, T>,
    mapper: Mapper<I, T>
):
    StoreImpl<K, I, T>(fetcher, cache, mapper), CrudStore<K, T> {

    protected val crudFetcher get() = fetcher as CrudFetcher

    override suspend fun create(entity: T): StoreResponse<T> {
        val createResult = crudFetcher.create(mapper.toFetcherEntity(entity))
        val storeResult = storeFetcherResult(createResult)
        return StoreResponse(storeResult, ResponseOrigin.FETCHER)
    }

    override suspend fun update(key: K, entity: T): StoreResponse<T> {
        val updateResult = crudFetcher.update(mapper.toFetcherEntity(entity))
        val storeResult = storeFetcherResult(updateResult)
        return StoreResponse(storeResult, ResponseOrigin.FETCHER)
    }

    override suspend fun delete(key: K, entity: T): Boolean {
        crudFetcher.delete(mapper.toFetcherEntity(entity))
        cache.delete(key)
        return true
    }

    private suspend fun storeFetcherResult(result: FetcherResult<I>): T {
        return when (result) {
            is FetcherResult.Data -> {
                val entityToSave = mapper.toCacheEntity(result.value)
                cache.store(buildKey(entityToSave), entityToSave)
            }
            is FetcherResult.Error -> throw result.error
            else -> throw Throwable("Error creating entity, invalid state")
        }
    }

}

/**
 * Simple CRUD store where the parsed fetcher entity type is the same as the cached entity type.
 */
abstract class SimpleCrudStoreImpl<K: Any, T: Any>(fetcher: CrudFetcher<K, T>, cache: Cache<K, T>):
    CrudStoreImpl<K, T, T>(fetcher, cache, SameEntityMapper())
