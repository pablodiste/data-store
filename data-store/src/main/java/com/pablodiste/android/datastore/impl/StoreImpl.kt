package com.pablodiste.android.datastore.impl

import com.pablodiste.android.datastore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

open class StoreImpl<K: Any, I: Any, T: Any>(
    protected val fetcher: Fetcher<K, I>,
    protected val cache: Cache<K, T>,
    protected val mapper: Mapper<I, T>
): Store<K, T> {

    private val TAG = this.javaClass.simpleName
    private val fetcherController = FetcherController(fetcher)

    /**
     * Finds and listen the cache for a entity. If the entity is not in cache if fetches it using the fetcher.
     * If there is anything in the cache, it emits it, otherwise it calls the fetcher.
     * @param refresh when true, if performs the fetch operation anyways, even if the data is cached.
     */
    override fun stream(key: K, refresh: Boolean): Flow<StoreResponse<T>> {
        return flow {
            coroutineScope {
                if (cache.exists(key)) {
                    val streamFlow = streamFromCache(key)
                    if (refresh) {
                        launch {
                            performFetch(key)
                        }
                    }
                    emitAll(streamFlow)
                } else {
                    emitAll(fetchAndStream(key))
                }
            }
        }
    }

    /**
     * This method will emit first the fetch results and then the cached results.
     * Please note the fetch results will come with entities disconnected from the DB at the beginning
     */
    private fun fetchAndStream(key: K): Flow<StoreResponse<T>> {
        return flow {
            emit(fetch(key, forced = false))
            emitAll(streamFromCache(key))
        }
    }

    /**
     * Loads an entity from cache (simple query), if it is not in cache, it calls the fetcher for it.
     */
    override suspend fun get(key: K): StoreResponse<T> {
        return if (cache.exists(key)) {
            val first = cache.get(key)
            StoreResponse(first, ResponseOrigin.CACHE)
        } else {
            fetch(key, forced = true)
        }
    }

    private fun streamFromCache(key: K) = cache.listen(key).map { StoreResponse(it, ResponseOrigin.CACHE) }

    /**
     * Fetches an entity from the fetcher. This call forces the API call.
     */
    override suspend fun fetch(key: K, forced: Boolean): StoreResponse<T> {
        val fetcherResult = fetcherController.fetch(key, forced)

        return withContext(Dispatchers.Main) {
            val result = when (fetcherResult) {
                is FetcherResult.Data -> {
                    val fetched = cache.store(key, mapper.toCacheEntity(fetcherResult.value), true)
                    fetched to ResponseOrigin.FETCHER
                }
                is FetcherResult.NoData -> {
                    val cacheResponse = cache.listen(key).first()
                    cacheResponse to ResponseOrigin.CACHE
                }
                is FetcherResult.Error -> throw fetcherResult.error
            }
            StoreResponse(result.first, result.second)
        }
    }

    /**
     * Subscribes to a call to fetch.
     * @param forced    if true, it ignores the rate limiter and makes the API call anyways.
     */
    suspend fun performFetch(key: K, forced: Boolean = false) = fetch(key, forced)

    open fun scoped(viewModelScope: CoroutineScope): ScopedStore<K, I, T> {
        return ScopedStore(fetcher, cache as ClosableCache, mapper, viewModelScope.coroutineContext)
    }
}

/**
 * Simple store where the parsed fetcher entity type is the same as the cached entity type.
 * Useful when parsing json over the DB objects directly.
 */
open class SimpleStoreImpl<K: Any, T: Any>(fetcher: Fetcher<K, T>, cache: Cache<K, T>):
    StoreImpl<K, T, T>(fetcher, cache, SameEntityMapper()) {

        override fun scoped(viewModelScope: CoroutineScope): ScopedSimpleStoreImpl<K, T> {
            return ScopedSimpleStoreImpl(fetcher, cache as ClosableCache, viewModelScope.coroutineContext)
        }

    }
