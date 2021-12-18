package com.pablodiste.android.datastore.impl

import com.pablodiste.android.datastore.Cache
import com.pablodiste.android.datastore.Fetcher
import com.pablodiste.android.datastore.Mapper
import com.pablodiste.android.datastore.SameEntityMapper
import kotlinx.coroutines.Job
import java.util.*
import kotlin.coroutines.CoroutineContext

open class ScopedStore<K: Any, I: Any, T: Any>(
    fetcher: Fetcher<K, I>,
    cache: ClosableCache<K, T>,
    mapper: Mapper<I, T>,
    coroutineContext: CoroutineContext
): StoreImpl<K, I, T>(fetcher, cache, mapper) {

    init {
        coroutineContext[Job]?.invokeOnCompletion {
            cache.closeableResourceManager.close()
        }
    }
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
    ScopedStore<K, T, T>(fetcher, cache, SameEntityMapper(), coroutineContext)