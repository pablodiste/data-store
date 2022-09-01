package dev.pablodiste.datastore.impl

import android.util.Log
import dev.pablodiste.datastore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

open class StoreImpl<K: Any, I: Any, T: Any>(
    protected open val fetcher: Fetcher<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    protected open val mapper: Mapper<I, T>
): Store<K, T> {

    protected var pausableSourceOfTruth: PausableSourceOfTruth<K, T> = PausableSourceOfTruth(sourceOfTruth)
    private val TAG = this.javaClass.simpleName
    private val fetcherController = FetcherController(fetcher)

    /**
     * Finds and listen the source of truth for a entity. If the entity is not in source of truth if fetches it using the fetcher.
     * If there is anything in the source of truth, it emits it, otherwise it calls the fetcher.
     * @param refresh when true, if performs the fetch operation anyways in parallel, even if the data is in the source of truth.
     */
    override fun stream(key: K, refresh: Boolean): Flow<StoreResponse<T>> {
        return flow {
            coroutineScope {
                if (pausableSourceOfTruth.exists(key)) {
                    val streamFlow = streamFromSourceOfTruth(key)
                    streamFlow.onEach { Log.d(TAG, "Coming from source of truth") }
                    if (refresh) {
                        val fetcherFlow = flow {
                            val fetched = performFetch(key)
                            // We do not emit the fetched data because we are listening reactively for updates in the streamFlow.
                            // We only emit errors
                            if (fetched !is StoreResponse.Data) {
                                Log.d(TAG, "Emitting from fetcher flow")
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
     * This method will emit first the fetch results and then the source of truth results.
     * Please note the fetch results will come with entities disconnected from the DB at the beginning
     */
    private fun fetchAndStream(key: K): Flow<StoreResponse<T>> {
        return flow {
            fetch(key, forced = false)
            // The result is not emitted directly but as a consequence of caching it.
            emitAll(streamFromSourceOfTruth(key))
        }
    }

    /**
     * Loads an entity from source of truth (simple query), if it is not in source of truth, it calls the fetcher for it.
     */
    override suspend fun get(key: K): StoreResponse<T> {
        return if (pausableSourceOfTruth.exists(key)) {
            val cached = pausableSourceOfTruth.get(key)
            StoreResponse.Data(cached, ResponseOrigin.SOURCE_OF_TRUTH)
        } else {
            fetch(key, forced = true)
        }
    }

    private fun streamFromSourceOfTruth(key: K) = pausableSourceOfTruth.listenWithResponse(key)

    /**
     * Fetches an entity from the fetcher and stores it in the source of truth.
     */
    override suspend fun fetch(key: K, forced: Boolean): StoreResponse<T> {

        val fetcherResult = fetcherController.fetch(key, forced)

        return withContext(Dispatchers.Main) {
            return@withContext when (fetcherResult) {
                is FetcherResult.Data -> {
                    Log.d(TAG, "Received Data")
                    if (fetcherResult.cacheable) {
                        val fetched = pausableSourceOfTruth.storeAfterFetch(key, mapper.toSourceOfTruthEntity(fetcherResult.value), true)
                        StoreResponse.Data(fetched, ResponseOrigin.FETCHER)
                    } else {
                        val sourceOfTruthResponse = pausableSourceOfTruth.get(key)
                        StoreResponse.Data(sourceOfTruthResponse, ResponseOrigin.SOURCE_OF_TRUTH)
                    }
                }
                is FetcherResult.NoData -> {
                    Log.d(TAG, "Received No Data")
                    StoreResponse.NoData(fetcherResult.message)
                    /*
                    // Returning cached data when the call is not made
                    val sourceOfTruthResponse = pausableSourceOfTruth.get(key)
                    Log.d(TAG, "Emitting cached data when there is no data")
                    StoreResponse.Data(sourceOfTruthResponse, ResponseOrigin.SOURCE_OF_TRUTH)
                    */
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
 * Simple store where the parsed fetcher entity type is the same as the source of truth entity type.
 * Useful when parsing json over the DB objects directly.
 */
open class SimpleStoreImpl<K: Any, T: Any>(fetcher: Fetcher<K, T>, sourceOfTruth: SourceOfTruth<K, T>):
    StoreImpl<K, T, T>(fetcher, sourceOfTruth, SameEntityMapper())

