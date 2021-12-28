package com.pablodiste.android.datastore.closable

import android.util.Log
import com.pablodiste.android.datastore.Cache
import com.pablodiste.android.datastore.Fetcher
import com.pablodiste.android.datastore.Mapper
import com.pablodiste.android.datastore.SameEntityMapper
import com.pablodiste.android.datastore.impl.StoreImpl
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class ScopedStore<K: Any, I: Any, T: Any>(
    fetcher: Fetcher<K, I>,
    override val cache: ClosableCache<K, T>,
    mapper: Mapper<I, T>
): StoreImpl<K, I, T>(fetcher, cache, mapper) {

    fun close() {
        Log.d("ScopedStore", "Releasing resources")
        cache.closeableResourceManager.close()
    }
}

fun <K: Any, I: Any, T: Any> CoroutineScope.launch(
    scopedStore: ScopedStore<K, I, T>,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    this.coroutineContext.job.invokeOnCompletion { scopedStore.close() }
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
                                                 cache: ClosableCache<K, T>,
                                                 coroutineContext: CoroutineContext):
    ScopedStore<K, T, T>(fetcher, cache, SameEntityMapper())