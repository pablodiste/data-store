package com.pablodiste.android.datastore.impl

import android.util.Log
import com.pablodiste.android.datastore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

open class StoreImpl<K: Any, I: Any, T: Any>(
    protected open val fetcher: Fetcher<K, I>,
    cache: Cache<K, T>,
    protected open val mapper: Mapper<I, T>
): Store<K, T> {

    protected var pausableCache: PausableCache<K, T> = PausableCache(cache)
    private val TAG = this.javaClass.simpleName
    private val fetcherController = FetcherController(fetcher)

    /**
     * Finds and listen the cache for a entity. If the entity is not in cache if fetches it using the fetcher.
     * If there is anything in the cache, it emits it, otherwise it calls the fetcher.
     * @param refresh when true, if performs the fetch operation anyways in parallel, even if the data is cached.
     */
    override fun stream(key: K, refresh: Boolean): Flow<StoreResponse<T>> {
        return flow {
            coroutineScope {
                if (pausableCache.exists(key)) {
                    val streamFlow = streamFromCache(key)
                    streamFlow.onEach { Log.d(TAG, "Coming from cache") }
                    if (refresh) {
                        val fetcherFlow = flow {
                            val fetched = performFetch(key)
                            // We do not emit the fetched data because we are listening reactively for updates in the streamFlow.
                            // We only emit errors
                            if (fetched !is StoreResponse.Data) {
                                emit(fetched)
                            } else {
                                Log.d(TAG, "Received data from fetcher, not emitting it")
                            }
                        }
                        emitAll(merge(streamFlow, fetcherFlow))
                    } else {
                        emitAll(streamFlow)
                    }
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
        return if (pausableCache.exists(key)) {
            val cached = pausableCache.get(key)
            StoreResponse.Data(cached, ResponseOrigin.CACHE)
        } else {
            fetch(key, forced = true)
        }
    }

    private fun streamFromCache(key: K) = pausableCache.listenWithResponse(key)

    /**
     * Fetches an entity from the fetcher. This call forces the API call.
     */
    override suspend fun fetch(key: K, forced: Boolean): StoreResponse<T> {

        val fetcherResult = fetcherController.fetch(key, forced)

        return withContext(Dispatchers.Main) {
            return@withContext when (fetcherResult) {
                is FetcherResult.Data -> {
                    if (fetcherResult.cacheable) {
                        val fetched = pausableCache.storeAfterFetch(key, mapper.toCacheEntity(fetcherResult.value), true)
                        StoreResponse.Data(fetched, ResponseOrigin.FETCHER)
                    } else {
                        val cacheResponse = pausableCache.get(key)
                        StoreResponse.Data(cacheResponse, ResponseOrigin.CACHE)
                    }
                }
                is FetcherResult.NoData -> {
                    Log.d(TAG, "Received No Data")
                    val cacheResponse = pausableCache.get(key)
                    StoreResponse.Data(cacheResponse, ResponseOrigin.CACHE)
                }
                is FetcherResult.Error -> StoreResponse.Error(fetcherResult.error)
            }
        }
    }

    /**
     * Subscribes to a call to fetch.
     * @param forced    if true, it ignores the rate limiter and makes the API call anyways.
     */
    suspend fun performFetch(key: K, forced: Boolean = false) = fetch(key, forced)
}

/**
 * Simple store where the parsed fetcher entity type is the same as the cached entity type.
 * Useful when parsing json over the DB objects directly.
 */
open class SimpleStoreImpl<K: Any, T: Any>(fetcher: Fetcher<K, T>, cache: Cache<K, T>):
    StoreImpl<K, T, T>(fetcher, cache, SameEntityMapper())

