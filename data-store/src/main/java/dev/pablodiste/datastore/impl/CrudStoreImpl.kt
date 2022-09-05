package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.crud.CrudOperation
import dev.pablodiste.datastore.crud.PendingWorker
import kotlinx.coroutines.CoroutineScope

open class CrudStoreImpl<K: Any, I: Any, T: Any>(
    applicationScope: CoroutineScope,
    fetcher: CrudFetcher<K, I>,
    val sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    private val keyBuilder: ((T) -> K)
):
    StoreImpl<K, I, T>(fetcher, sourceOfTruth, mapper), CrudStore<K, T> {

    protected val pendingWorker = PendingWorker(applicationScope, fetcher, sourceOfTruth, mapper, keyBuilder)
    protected val crudFetcher get() = fetcher as CrudFetcher

    override suspend fun create(key: K, entity: T): StoreResponse<T> {
        val stored = sourceOfTruth.store(key, entity)
        pendingWorker.submitChange(CrudOperation.CREATE, key, mapper.toFetcherEntity(entity))
        return StoreResponse.Data(stored, ResponseOrigin.SOURCE_OF_TRUTH)
    }

    override suspend fun update(key: K, entity: T): StoreResponse<T> {
        val stored = sourceOfTruth.store(key, entity)
        pendingWorker.submitChange(CrudOperation.UPDATE, key, mapper.toFetcherEntity(entity))
        return StoreResponse.Data(stored, ResponseOrigin.SOURCE_OF_TRUTH)
    }

    override suspend fun delete(key: K, entity: T): Boolean {
        sourceOfTruth.delete(key)
        pendingWorker.submitChange(CrudOperation.DELETE, key, mapper.toFetcherEntity(entity))
        return true
    }

    override fun dispose() {
        pendingWorker.dispose()
    }
}

/**
 * Simple CRUD store where the parsed fetcher entity type is the same as the source of truth entity type.
 */
open class SimpleCrudStoreImpl<K: Any, T: Any>(
    applicationScope: CoroutineScope,
    fetcher: CrudFetcher<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>,
    keyBuilder: ((T) -> K)
): CrudStoreImpl<K, T, T>(applicationScope, fetcher, sourceOfTruth, SameEntityMapper(), keyBuilder)
