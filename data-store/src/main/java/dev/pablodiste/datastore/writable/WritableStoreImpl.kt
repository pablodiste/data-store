package dev.pablodiste.datastore.writable

import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.impl.StoreImpl
import kotlinx.coroutines.CoroutineScope

open class WritableStoreImpl<K: Any, I: Any, T: Any>(
    clazz: Class<T>,
    applicationScope: CoroutineScope,
    fetcher: Fetcher<K, I>,
    val sender: Sender<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    val keyBuilder: (T) -> K
): StoreImpl<K, I, T>(fetcher, sourceOfTruth, mapper), WritableStore<K, T> {

    protected val worker = WorkerManager.getOrCreateWorker(clazz, applicationScope, sourceOfTruth, mapper, keyBuilder)

    override suspend fun put(key: K, entity: T, change: EntityChange<T>, operation: ChangeOperation): StoreResponse<T> {
        val updatedEntity = change(entity)
        val stored = sourceOfTruth.store(key, updatedEntity)
        worker.queueChange(PendingChange(key, entity, change, operation, sender))
        return StoreResponse.Data(stored, ResponseOrigin.SOURCE_OF_TRUTH)
    }

    override suspend fun remove(key: K, entity: T): StoreResponse<Boolean> {
        sourceOfTruth.delete(key)
        worker.queueChange(PendingChange(key, entity, { entity }, ChangeOperation.DELETE, sender))
        return StoreResponse.Data(true, origin = ResponseOrigin.SOURCE_OF_TRUTH)
    }

}

class SimpleWritableStoreImpl<K: Any, T: Any>(
    clazz: Class<T>,
    applicationScope: CoroutineScope,
    fetcher: Fetcher<K, T>,
    sender: Sender<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>,
    keyBuilder: (T) -> K
): WritableStoreImpl<K, T, T>(clazz, applicationScope, fetcher, sender, sourceOfTruth, SameEntityMapper(), keyBuilder)


suspend fun <K: Any, T: Any> WritableStore<K, T>.create(key: K, entity: T): StoreResponse<T> = put(
    key = key,
    entity = entity,
    change = { entity },
    operation = ChangeOperation.CREATE
)

suspend fun <K: Any, T: Any> WritableStore<K, T>.update(key: K, entity: T, change: EntityChange<T>): StoreResponse<T> = put(
    key = key,
    entity = entity,
    change = change,
    operation = ChangeOperation.UPDATE
)
