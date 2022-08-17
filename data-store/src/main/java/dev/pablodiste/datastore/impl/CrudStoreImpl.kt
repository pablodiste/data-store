package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.*

open class CrudStoreImpl<K: Any, I: Any, T: Any>(
    fetcher: CrudFetcher<K, I>,
    cache: Cache<K, T>,
    mapper: Mapper<I, T>,
    private val keyBuilder: ((T) -> K)? = null
):
    StoreImpl<K, I, T>(fetcher, cache, mapper), CrudStore<K, T> {

    protected val crudFetcher get() = fetcher as CrudFetcher

    override suspend fun create(key: K, entity: T): StoreResponse<T> {
        return try {
            val createResult = crudFetcher.create(key, mapper.toFetcherEntity(entity))
            val storeResult = storeFetcherResult(createResult)
            StoreResponse.Data(storeResult, ResponseOrigin.FETCHER)
        } catch (e: Exception) {
            StoreResponse.Error(e)
        }
    }

    override suspend fun update(key: K, entity: T): StoreResponse<T> {
        return try {
            val updateResult = crudFetcher.update(key, mapper.toFetcherEntity(entity))
            val storeResult = storeFetcherResult(updateResult)
            StoreResponse.Data(storeResult, ResponseOrigin.FETCHER)
        } catch (e: Exception) {
            StoreResponse.Error(e)
        }
    }

    override suspend fun delete(key: K, entity: T): Boolean {
        return try {
            crudFetcher.delete(key, mapper.toFetcherEntity(entity))
            pausableCache.delete(key)
            true
        } catch (e: Exception) {
            false
        }
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
        return keyBuilder?.let { it(entity) } ?:
            throw IllegalStateException("Override this method or provide a keyBuilder to build the Key for CRUD operations")
    }

}

/**
 * Simple CRUD store where the parsed fetcher entity type is the same as the cached entity type.
 */
open class SimpleCrudStoreImpl<K: Any, T: Any>(
    fetcher: CrudFetcher<K, T>,
    cache: Cache<K, T>,
    keyBuilder: ((T) -> K)? = null
): CrudStoreImpl<K, T, T>(fetcher, cache, SameEntityMapper(), keyBuilder)
