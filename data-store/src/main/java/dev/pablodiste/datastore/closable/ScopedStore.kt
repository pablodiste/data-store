package dev.pablodiste.datastore.closable

import android.util.Log
import dev.pablodiste.datastore.Cache
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.Mapper
import dev.pablodiste.datastore.SameEntityMapper
import dev.pablodiste.datastore.impl.StoreImpl
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class ScopedStore<K: Any, I: Any, T: Any>(
    fetcher: Fetcher<K, I>,
    private val cache: ClosableCache<K, T>,
    mapper: Mapper<I, T>
): StoreImpl<K, I, T>(fetcher, cache, mapper) {

    fun autoClose(coroutineScope: CoroutineScope) {
        autoClose(coroutineScope.coroutineContext)
    }

    fun autoClose(coroutineContext: CoroutineContext) {
        autoClose(coroutineContext.job)
    }

    fun autoClose(job: Job) {
        job.invokeOnCompletion { close() }
    }

    fun close() {
        Log.d("ScopedStore", "Releasing resources")
        cache.closeableResourceManager.close()
    }
}

fun Job.autoClose(vararg scopedStores: ScopedStore<*, *, *>) {
    scopedStores.forEach { it.autoClose(this) }
}

fun <K: Any, I: Any, T: Any> CoroutineScope.launch(
    scopedStore: ScopedStore<K, I, T>,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    scopedStore.autoClose(this)
    return launch(context, start, block)
}

abstract class ClosableCache<K: Any, T: Any>(val closeableResourceManager: CloseableResourceManager = CloseableResourceManager()): Cache<K, T>

typealias CloseableResourceListener = () -> Unit

class CloseableResourceManager {

    private val closeableResourceListeners: MutableList<CloseableResourceListener> = Collections.synchronizedList(mutableListOf())

    fun addOnCloseListener(closableResourceListener: () -> Unit) {
        closeableResourceListeners.add(closableResourceListener)
    }

    fun close() {
        synchronized(this) {
            closeableResourceListeners.forEach { it.invoke() }
            closeableResourceListeners.clear()
        }
    }
}

/**
 * Simple store where the parsed fetcher entity type is the same as the cached entity type.
 * Useful when parsing json over the DB objects directly.
 */
open class ScopedSimpleStoreImpl<K: Any, T: Any>(fetcher: Fetcher<K, T>,
                                                 cache: ClosableCache<K, T>):
    ScopedStore<K, T, T>(fetcher, cache, SameEntityMapper())