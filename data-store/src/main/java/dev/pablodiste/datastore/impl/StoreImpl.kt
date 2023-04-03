package dev.pablodiste.datastore.impl

import android.util.Log
import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.fetchers.FetcherController
import dev.pablodiste.datastore.fetchers.FetcherException
import dev.pablodiste.datastore.writable.EntityStoreGroup
import dev.pablodiste.datastore.writable.GroupableStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

                var sourceOfTruthFlow = streamFromSourceOfTruth(request.key)
                    .onEach { Log.d(TAG, "Emit from SOT") }
                val exist = pausableSourceOfTruth.exists(request.key)
                val shouldFetch = (exist && request.refresh) || (!exist && request.fetchWhenNoDataFound)
                val fetcherFlow = streamFromFetcher(this, request, shouldFetch)
                    .onEach { Log.d(TAG, "Emit from Fetcher") }

                if (exist && request.refresh) {
                    emit(sourceOfTruthFlow.onEach { Log.d(TAG, "Coming from source of truth (first)") }.first())
                    sourceOfTruthFlow = sourceOfTruthFlow.drop(1).onEach { Log.d(TAG, "Coming from source of truth (rest)") }
                }
                emitAll(merge(sourceOfTruthFlow, fetcherFlow))
            }
        }
    }

    /**
     * Loads an entity from source of truth (simple query), if it is not in source of truth, it calls the fetcher for it.
     */
    override suspend fun get(request: StoreRequest<K>): StoreResponse<T> {
        return coroutineScope {
            if (pausableSourceOfTruth.exists(request.key)) {
                val cached = pausableSourceOfTruth.get(request.key)
                StoreResponse.Data(cached, ResponseOrigin.SOURCE_OF_TRUTH)
            } else {
                if (request.fetchWhenNoDataFound) {
                    performFetch(request.key, request.forceFetch)
                    //streamFromFetcher(this, request, shouldFetch = true).first()
                } else {
                    StoreResponse.Error(IllegalStateException("No data found"))
                }
            }
        }
    }

    private fun streamFromSourceOfTruth(key: K) = pausableSourceOfTruth.listenWithResponse(key)

    /**
     * Fetches an entity from the fetcher and stores it in the source of truth.
     */
    override suspend fun fetch(request: StoreRequest<K>): StoreResponse<T> {

        val fetcherResult = fetcherController.fetch(request.key, request.forceFetch, request.emitLoadingStates)

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
                is FetcherResult.Error -> StoreResponse.Error(FetcherException(fetcherResult.error))
                is FetcherResult.Success -> StoreResponse.Error(IllegalStateException("Unexpected fetcher result"))
                is FetcherResult.Loading -> StoreResponse.Loading(ResponseOrigin.FETCHER)
            }
        }
    }

    /**
     * Subscribes to a call to fetch.
     * @param forced    if true, it ignores the rate limiter and makes the API call anyways.
     */
    suspend fun performFetch(key: K, forced: Boolean = false) = fetch(StoreRequest(key, forceFetch = forced))

    /**
     * Executes the fetcher call and emits the fetcher result and loading states into a flow.
     */
    private fun streamFromFetcher(
        coroutineScope: CoroutineScope,
        request: StoreRequest<K>,
        shouldFetch: Boolean): Flow<StoreResponse<T>> = flow {
        Log.d(TAG, "Collecting from fetcher flow")
        val fetcherFlow = fetcherController.getFetcherFlow(request.key)
            .onSubscription {
                coroutineScope.launch {
                    if (shouldFetch) {
                        fetch(request)
                    }
                }
            }
            .onEach { Log.d(TAG, "Fetcher Flow: $it") }
            .map { fetcherResult ->
                when (fetcherResult) {
                    is FetcherResult.Loading -> StoreResponse.Loading(ResponseOrigin.FETCHER)
                    is FetcherResult.Data -> null// This case is used when the cached data comes from the SOT, we don't have to emit here.
                    is FetcherResult.NoData -> if (request.emitNoDataStates) StoreResponse.NoData(fetcherResult.message) else null
                    is FetcherResult.Error -> StoreResponse.Error(FetcherException(fetcherResult.error))
                    is FetcherResult.Success -> StoreResponse.Error(IllegalStateException("Unexpected fetcher result"))
                }
            }
            .filterNotNull()
            .onEach { Log.d(TAG, "Emitting from fetcher flow") }

        emitAll(fetcherFlow)
    }
}

/**
 * Simple store where the parsed fetcher entity type is the same as the source of truth entity type.
 * Useful when parsing json over the DB objects directly.
 */
open class SimpleStoreImpl<K: Any, T: Any>(fetcher: Fetcher<K, T>, sourceOfTruth: SourceOfTruth<K, T>):
    StoreImpl<K, T, T>(fetcher, sourceOfTruth, SameEntityMapper())
