package dev.pablodiste.datastore.crud

import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.CrudStore
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.Mapper
import dev.pablodiste.datastore.ResponseOrigin
import dev.pablodiste.datastore.SameEntityMapper
import dev.pablodiste.datastore.SourceOfTruth
import dev.pablodiste.datastore.StoreResponse
import dev.pablodiste.datastore.impl.StoreImpl

open class CrudStoreImpl<K: Any, I: Any, T: Any>(
    protected val crudFetcher: CrudFetcher<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    private val keyBuilder: ((T) -> K)
): StoreImpl<K, I, T>(crudFetcher.readFetcher, sourceOfTruth, mapper), CrudStore<K, T> {

    override suspend fun create(key: K, entity: T): StoreResponse<T> {
        return try {
            val createResult = crudFetcher.createSender.send(key, mapper.toFetcherEntity(entity))
            val storeResult = storeFetcherResult(createResult)
            StoreResponse.Data(storeResult, ResponseOrigin.FETCHER)
        } catch (e: Exception) {
            StoreResponse.Error(e)
        }
    }

    override suspend fun update(key: K, entity: T): StoreResponse<T> {
        return try {
            val updateResult = crudFetcher.updateSender.send(key, mapper.toFetcherEntity(entity))
            val storeResult = storeFetcherResult(updateResult)
            StoreResponse.Data(storeResult, ResponseOrigin.FETCHER)
        } catch (e: Exception) {
            StoreResponse.Error(e)
        }
    }

    override suspend fun delete(key: K, entity: T): Boolean {
        return try {
            crudFetcher.deleteSender.send(key, mapper.toFetcherEntity(entity))
            sourceOfTruth.delete(key)
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun storeFetcherResult(result: FetcherResult<I>): T {
        return when (result) {
            is FetcherResult.Data -> {
                val entityToSave = mapper.toSourceOfTruthEntity(result.value)
                sourceOfTruth.store(keyBuilder(entityToSave), entityToSave)
            }
            is FetcherResult.Error -> throw result.error.exception
            else -> throw Throwable("Error creating entity, invalid state")
        }
    }

    override fun dispose() { }

}

/**
 * Simple CRUD store where the parsed fetcher entity type is the same as the source of truth entity type.
 */
open class SimpleCrudStoreImpl<K: Any, T: Any>(
    crudFetcher: CrudFetcher<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>,
    keyBuilder: ((T) -> K)
): CrudStoreImpl<K, T, T>(crudFetcher, sourceOfTruth, SameEntityMapper(), keyBuilder)
