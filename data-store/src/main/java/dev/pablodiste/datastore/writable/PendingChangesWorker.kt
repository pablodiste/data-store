package dev.pablodiste.datastore.writable

import dev.pablodiste.datastore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
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

    private val pendingItemsFlow = MutableSharedFlow<PendingChange<K, T, I>>(replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.SUSPEND)
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
                        // Implement failure strategy
                        delay(1000)
                    }
                }
            }
        }
    }

    fun queueChange(pendingChange: PendingChange<K, T, I>) {
        pendingItems.add(pendingChange)
        pendingItemsFlow.tryEmit(pendingChange)
    }

    fun dispose() {
        workerJob?.cancel()
    }
}