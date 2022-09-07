package dev.pablodiste.datastore.writable

import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.impl.StoreImpl
import kotlinx.coroutines.CoroutineScope

typealias EntityChange<T> = (T) -> T

enum class ChangeOperation {
    CREATE, UPDATE, DELETE
}

interface WritableStore<K: Any, T: Any>: Store<K, T> {
    suspend fun put(key: K, entity: T, change: EntityChange<T>, operation: ChangeOperation): StoreResponse<T>
    fun dispose()
}

open class WritableStoreImpl<K: Any, I: Any, T: Any>(
    applicationScope: CoroutineScope,
    fetcher: Fetcher<K, I>,
    sender: Sender<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    val keyBuilder: (T) -> K
): StoreImpl<K, I, T>(fetcher, sourceOfTruth, mapper), WritableStore<K, T> {

    protected val worker = PendingChangesWorker(applicationScope, sender, sourceOfTruth, mapper, keyBuilder)

    override suspend fun put(key: K, entity: T, change: EntityChange<T>, operation: ChangeOperation): StoreResponse<T> {
        val updatedEntity = change(entity)
        val stored = sourceOfTruth.store(key, updatedEntity)
        worker.queueChange(PendingChange(key, entity, change, operation))
        return StoreResponse.Data(stored, ResponseOrigin.SOURCE_OF_TRUTH)
    }

    override fun dispose() {
        worker.dispose()
    }

}

class SimpleWritableStoreImpl<K: Any, T: Any>(
    applicationScope: CoroutineScope,
    fetcher: Fetcher<K, T>,
    sender: Sender<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>,
    keyBuilder: (T) -> K
): WritableStoreImpl<K, T, T>(applicationScope, fetcher, sender, sourceOfTruth, SameEntityMapper(), keyBuilder)


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

suspend fun <K: Any, T: Any> WritableStore<K, T>.delete(key: K, entity: T): StoreResponse<T> = put(
    key = key,
    entity = entity,
    change = { entity },
    operation = ChangeOperation.DELETE
)