package dev.pablodiste.datastore.crud

import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.Mapper
import dev.pablodiste.datastore.SourceOfTruth
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class PendingWorker<K: Any, T: Any, I: Any>(
    applicationScope: CoroutineScope,
    pendingWorkerFetcher: CrudFetcher<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    private val keyBuilder: ((T) -> K)
) {

    private val pendingItems = MutableSharedFlow<PendingCrudJob<K, I>>(replay = 0, extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.SUSPEND)
    private val workerJob: Job

    init {
        workerJob = applicationScope.launch(Dispatchers.IO) {
            pendingItems.collect {
                var success = false
                while (!success) {
                    val result = when (it.crudOperation) {
                        CrudOperation.CREATE -> pendingWorkerFetcher.create(it.key, it.entity)
                        CrudOperation.UPDATE -> pendingWorkerFetcher.update(it.key, it.entity)
                        CrudOperation.DELETE -> pendingWorkerFetcher.delete(it.key, it.entity)
                    }
                    if (result is FetcherResult.Data || (result is FetcherResult.Success && result.success)) {
                        if (result is FetcherResult.Data) {
                            if (it.crudOperation == CrudOperation.CREATE) sourceOfTruth.delete(it.key)
                            val newData = mapper.toSourceOfTruthEntity(result.value)
                            sourceOfTruth.store(keyBuilder(newData), newData)
                        }
                        success = true
                    } else {
                        delay(1000)
                    }
                }
            }
        }
    }

    fun submitChange(crudOperation: CrudOperation, key: K, entity: I) {
        pendingItems.tryEmit(PendingCrudJob(crudOperation, key, entity))
    }

    fun dispose() {
        workerJob.cancel()
    }
}