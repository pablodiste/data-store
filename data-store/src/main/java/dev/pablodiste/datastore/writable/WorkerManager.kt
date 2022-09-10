package dev.pablodiste.datastore.writable

import dev.pablodiste.datastore.Mapper
import dev.pablodiste.datastore.SourceOfTruth
import dev.pablodiste.datastore.Store
import kotlinx.coroutines.CoroutineScope

object WorkerManager {

    private val workers: MutableMap<Class<*>, PendingChangesWorker<*, *, *>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    fun <K: Any, I: Any, T: Any> getOrCreateWorker(
        store: Store<K, T>,
        clazz: Class<*>,
        applicationScope: CoroutineScope,
        sourceOfTruth: SourceOfTruth<K, T>,
        mapper: Mapper<I, T>,
        keyBuilder: ((T) -> K)
    ): PendingChangesWorker<K, T, I> {
        return ((workers[clazz] ?: PendingChangesWorker(applicationScope, sourceOfTruth, mapper, keyBuilder)
            .also { workers[clazz] = it }
        ) as PendingChangesWorker<K, T, I>)
    }

    @Suppress("UNCHECKED_CAST")
    fun <K: Any, I: Any, T: Any> getWorker(store: Store<K, T>, clazz: Class<*>): PendingChangesWorker<K, T, I>? {
        return (workers[clazz] as? PendingChangesWorker<K, T, I>)
    }

    fun dispose() {
        workers.values.forEach { it.dispose() }
        workers.clear()
    }

}