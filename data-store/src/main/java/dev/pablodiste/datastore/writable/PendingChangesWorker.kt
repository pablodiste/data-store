package dev.pablodiste.datastore.writable

import dev.pablodiste.datastore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow

data class PendingChange<K: Any, T: Any, I: Any>(
    val key: K,
    val entity: T,
    val change: EntityChange<T>,
    val changeOperation: ChangeOperation,
    val pendingWorkerSender: Sender<K, I>,
)

class PendingChangesWorker<K: Any, T: Any, I: Any>(
    private val applicationScope: CoroutineScope,
    private val sourceOfTruth: SourceOfTruth<K, T>,
    private val mapper: Mapper<I, T>,
    private val keyBuilder: ((T) -> K)
) {

    private val pendingItemsFlow = MutableSharedFlow<PendingChange<K, T, I>>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.SUSPEND)
    private val pendingItems = mutableListOf<PendingChange<K, T, I>>()
    private var workerJob: Job? = null

    init {
        startJob()
    }

    private fun startJob() {
        workerJob = applicationScope.launch(Dispatchers.IO) {
            pendingItemsFlow.collect {
                var success = false
                while (!success) {
                    val entityToSend = mapper.toFetcherEntity(it.change(it.entity))
                    val result = it.pendingWorkerSender.send(it.key, entityToSend, it.changeOperation)

                    if (result is FetcherResult.Data || (result is FetcherResult.Success && result.success)) {
                        if (result is FetcherResult.Data) {
                            if (it.changeOperation == ChangeOperation.CREATE) sourceOfTruth.delete(it.key)
                            val newData = mapper.toSourceOfTruthEntity(result.value)
                            sourceOfTruth.store(keyBuilder(newData), newData)
                        }
                        success = true
                        pendingItems.remove(it)
                    } else {
                        // TODO: Implement failure strategy
                        delay(1000)
                    }
                }
            }
        }
    }

    fun queueChange(pendingChange: PendingChange<K, T, I>) {
        if (pendingChange.changeOperation == ChangeOperation.DELETE) {
            removeAllChangesWithKey(pendingChange.key)
        }
        pendingItems.add(pendingChange)
        pendingItemsFlow.tryEmit(pendingChange)
        pendingItemsFlow.replayCache
    }

    private fun removeAllChangesWithKey(key: K) {
        // TODO Not yet implemented
    }

    fun dispose() {
        workerJob?.cancel()
    }

    fun applyPendingChanges(key: K, entity: T): PendingChangesApplicationResult<T> {
        var changingEntity = entity
        var shouldStoreIt = true
        pendingItems.filter { it.key == key }.forEach {
            when (it.changeOperation) {
                // This happens when you create something on the client and it was also created on the server with the same key,
                // this is unlikely to happen if the key does not clash. We give priority to local submission and we ignore fetched one.
                ChangeOperation.CREATE -> { changingEntity = it.entity; shouldStoreIt = false }
                // For updates we reapply the pending changes to fetched data.
                ChangeOperation.UPDATE -> changingEntity = it.change(entity)
                // If it was deleted locally, we ignore incoming data.
                ChangeOperation.DELETE -> { changingEntity = it.entity; shouldStoreIt = false }
            }
        }
        return PendingChangesApplicationResult(changingEntity, shouldStoreIt)
    }
}

data class PendingChangesApplicationResult<T: Any>(val updatedEntity: T, val shouldStoreIt: Boolean)
