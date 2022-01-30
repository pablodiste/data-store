package com.pablodiste.android.datastore.adapters.room

interface StalenessPolicy<K: Any, T: Any> {
    suspend fun removeStaleEntity(cache: RoomCache<K, T>, key: K, entity: T)
    suspend fun removeStaleList(cache: RoomListCache<K, T>, key: K, entity: List<T>)
}

/**
 * This staleness policy does not delete any entity.
 */
class DoNotExpireStalenessPolicy<K: Any, T: Any>: StalenessPolicy<K, T> {
    override suspend fun removeStaleList(cache: RoomListCache<K, T>, key: K, entity: List<T>) {
    }

    override suspend fun removeStaleEntity(cache: RoomCache<K, T>, key: K, entity: T) {
    }
}

/**
 * This staleness policy executes the repository query and deletes all entities that matched.
 */
class DeleteAllStalenessPolicy<K: Any, T: Any>: StalenessPolicy<K, T> {

    override suspend fun removeStaleEntity(cache: RoomCache<K, T>, key: K, entity: T) {
        cache.delete(key)
    }

    override suspend fun removeStaleList(cache: RoomListCache<K, T>, key: K, entity: List<T>) {
        cache.delete(key)
    }
}

/**
 * This staleness policy removes all stored entities which are not included in the API response
 * Requires the entities to implement HasKey interface to compare by the keys
 */
class DeleteAllNotInFetchStalenessPolicy<K: Any, T: Any>(
    private val keyBuilder: ((T) -> K)): StalenessPolicy<K, T> {

    override suspend fun removeStaleEntity(cache: RoomCache<K, T>, key: K, entity: T) {
        cache.delete(key)
    }

    override suspend fun removeStaleList(cache: RoomListCache<K, T>, key: K, entity: List<T>) {
        // Collects all keys from API call
        val inputKeys = entity.mapTo(HashSet()) { i -> keyBuilder(i) }
        val cached = cache.get(key)
        cached.forEach { storedEntity ->
            // if the store contains an entity not in the API response, it gets deleted
            val storedKey = keyBuilder(storedEntity)
            if (!inputKeys.contains(storedKey)) {
                cache.delete(storedKey)
            }
        }
    }
}

