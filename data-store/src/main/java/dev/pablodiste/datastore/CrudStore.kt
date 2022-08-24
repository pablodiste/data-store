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

    /**
     * Provides a way to extract the key from a just created entity, in order to save it in the store
     */
    fun buildKey(entity: T): K

}

interface CrudFetcher<K: Any, I: Any>: Fetcher<K, I> {
    suspend fun create(key: K, entity: I): FetcherResult<I>
    suspend fun update(key: K, entity: I): FetcherResult<I>
    suspend fun delete(key: K, entity: I): Boolean
}
