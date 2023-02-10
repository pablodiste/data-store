package dev.pablodiste.datastore

interface CrudStore<K: Any, T: Any>: Store<K, T> {
    /**
     * Performs a create operation (usually a HTTP POST), and stores it in the source of truth
     * @return the source of truth entity
     */
    suspend fun create(key: K, entity: T): StoreResponse<T>

    /**
     * Performs an update operation (usually a HTTP PUT), and updates it in the source of truth
     * @return the source of truth entity
     */
    suspend fun update(key: K, entity: T): StoreResponse<T>

    /**
     * Performs a delete/remove operation (usually a HTTP DELETE), and removes it from the source of truth
     * @return the source of truth entity
     */
    suspend fun delete(key: K, entity: T): Boolean

    fun dispose()
}

class CrudFetcher<K: Any, I: Any>(
    val readFetcher: Fetcher<K, I>,
    val createSender: Sender<K, I> = Sender { _, _ -> FetcherResult.NoData("NOOP") },
    val updateSender: Sender<K, I> = Sender { _, _ -> FetcherResult.NoData("NOOP") },
    val deleteSender: Sender<K, I> = Sender { _, _ -> FetcherResult.NoData("NOOP") }
)