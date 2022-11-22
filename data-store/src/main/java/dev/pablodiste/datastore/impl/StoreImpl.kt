package dev.pablodiste.datastore.impl

import android.util.Log
import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.writable.EntityStoreGroup
import dev.pablodiste.datastore.writable.GroupableStore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

open class StoreImpl<K: Any, I: Any, T: Any>(
    protected open val fetcher: Fetcher<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    protected open val mapper: Mapper<I, T>
): Store<K, T>, GroupableStore<T> {

    protected var pausableSourceOfTruth: PausableSourceOfTruth<K, T> = PausableSourceOfTruth(sourceOfTruth)
    private val TAG = this.javaClass.simpleName
    private val fetcherController = FetcherController(fetcher)

    protected val sourceOfTruth: SourceOfTruth<K, T> get() = pausableSourceOfTruth

    protected val coroutineConfig get() = StoreConfig.coroutineConfig

    override var group: EntityStoreGroup<T>? = null

    /**
     * Finds and listen the source of truth for a entity. If the entity is not in source of truth if fetches it using the fetcher.
     * If there is anything in the source of truth, it emits it, otherwise it calls the fetcher.
     * @param refresh when true, if performs the fetch operation anyways in parallel, even if the data is in the source of truth.
     */
    override fun stream(request: StoreRequest<K>): Flow<StoreResponse<T>> {
        return flow {
            coroutineScope {
                if (pausableSourceOfTruth.exists(request.key)) {
                    val streamFlow = streamFromSourceOfTruth(request.key)
                    streamFlow.onEach { Log.d(TAG, "Coming from source of truth") }
                    if (request.refresh) {
                        val fetcherFlow = flow {
                            val fetched = performFetch(request.key)
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
                    if (request.fetchWhenNoDataFound) {
                        emitAll(fetchAndStream(request.key))
                    } else {
                        emitAll(streamFromSourceOfTruth(request.key))
                    }
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
    override suspend fun get(request: StoreRequest<K>): StoreResponse<T> {
        return if (pausableSourceOfTruth.exists(request.key)) {
            val cached = pausableSourceOfTruth.get(request.key)
            StoreResponse.Data(cached, ResponseOrigin.SOURCE_OF_TRUTH)
        } else {
            if (request.fetchWhenNoDataFound) {
                fetch(request.key, forced = true)
            } else {
                StoreResponse.Error(IllegalStateException("No data found"))
            }
        }
    }

    private fun streamFromSourceOfTruth(key: K) = pausableSourceOfTruth.listenWithResponse(key)

    /**
     * Fetches an entity from the fetcher and stores it in the source of truth.
     */
    override suspend fun fetch(request: StoreRequest<K>): StoreResponse<T> {

        val fetcherResult = fetcherController.fetch(request.key, request.forceFetch)

        return withContext(coroutineConfig.mainDispatcher) {
            return@withContext when (fetcherResult) {
                is FetcherResult.Data -> {
                    Log.d(TAG, "Received Data")
                    if (fetcherResult.cacheable) {
                        val sourceOfTruthEntity = mapper.toSourceOfTruthEntity(fetcherResult.value)

                        // If there are pending write operations, they are applied to the incoming fetched data.
                        val pendingUpdatesResult = group?.applyPendingChanges<K, I>(request.key, sourceOfTruthEntity)
                        val entityToStore = pendingUpdatesResult?.updatedEntity ?: sourceOfTruthEntity
                        val shouldStoreIt = pendingUpdatesResult?.shouldStoreIt ?: true

                        // In some case like pending local deletions, we should not store fetched data.
                        if (shouldStoreIt) {
                            val fetched = pausableSourceOfTruth.storeAfterFetch(request.key, entityToStore, true)
                            StoreResponse.Data(fetched, ResponseOrigin.FETCHER)
                        } else {
                            StoreResponse.NoData("Ignored because of pending operations in queue")
                        }
                    } else {
                        val sourceOfTruthResponse = pausableSourceOfTruth.listen(request.key).first()
                        StoreResponse.Data(sourceOfTruthResponse, ResponseOrigin.SOURCE_OF_TRUTH)
                    }
                }
                is FetcherResult.NoData -> {
                    Log.d(TAG, "Received No Data")
                    StoreResponse.NoData(fetcherResult.message)
                    /*
                    TODO: Maybe enable this behavior via a configuration
                    // Returning cached data when the call is not made
                    val sourceOfTruthResponse = pausableSourceOfTruth.get(key)
                    Log.d(TAG, "Emitting cached data when there is no data")
                    StoreResponse.Data(sourceOfTruthResponse, ResponseOrigin.SOURCE_OF_TRUTH)
                    */
                }
                is FetcherResult.Error -> StoreResponse.Error(fetcherResult.error.exception)
                is FetcherResult.Success -> StoreResponse.Error(IllegalStateException("Unexpected fetcher result"))
            }
        }
    }

    /**
     * Subscribes to a call to fetch.
     * @param forced    if true, it ignores the rate limiter and makes the API call anyways.
     */
    suspend fun performFetch(key: K, forced: Boolean = false) = fetch(StoreRequest(key, forceFetch = forced))
}

/**
 * Simple store where the parsed fetcher entity type is the same as the source of truth entity type.
 * Useful when parsing json over the DB objects directly.
 */
open class SimpleStoreImpl<K: Any, T: Any>(fetcher: Fetcher<K, T>, sourceOfTruth: SourceOfTruth<K, T>):
    StoreImpl<K, T, T>(fetcher, sourceOfTruth, SameEntityMapper())
