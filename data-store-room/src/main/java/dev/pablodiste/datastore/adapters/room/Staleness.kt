package dev.pablodiste.datastore.adapters.room

interface StalenessPolicy<K: Any, T: Any> {
    suspend fun removeStaleEntity(sourceOfTruth: RoomSourceOfTruth<K, T>, key: K, entity: T)
    suspend fun removeStaleList(sourceOfTruth: RoomListSourceOfTruth<K, T>, key: K, entity: List<T>)
}

/**
 * This staleness policy does not delete any entity.
 */
class DoNotExpireStalenessPolicy<K: Any, T: Any>: StalenessPolicy<K, T> {
    override suspend fun removeStaleList(sourceOfTruth: RoomListSourceOfTruth<K, T>, key: K, entity: List<T>) {}
    override suspend fun removeStaleEntity(sourceOfTruth: RoomSourceOfTruth<K, T>, key: K, entity: T) {}
}

/**
 * This staleness policy executes the repository query and deletes all entities that matched.
 */
class DeleteAllStalenessPolicy<K: Any, T: Any>: StalenessPolicy<K, T> {

    override suspend fun removeStaleEntity(sourceOfTruth: RoomSourceOfTruth<K, T>, key: K, entity: T) {
        sourceOfTruth.delete(key)
    }

    override suspend fun removeStaleList(sourceOfTruth: RoomListSourceOfTruth<K, T>, key: K, entity: List<T>) {
        sourceOfTruth.delete(key)
    }
}

/**
 * This staleness policy removes all stored entities which are not included in the API response
 * @param keyBuilder should return a primary id of the provided entity like { entity -> entity.id }
 */
class DeleteAllNotInFetchStalenessPolicy<K: Any, T: Any, PK: Any>(
    private val keyBuilder: ((T) -> PK)): StalenessPolicy<K, T> {

    override suspend fun removeStaleEntity(sourceOfTruth: RoomSourceOfTruth<K, T>, key: K, entity: T) {
        sourceOfTruth.delete(key)
    }

    override suspend fun removeStaleList(sourceOfTruth: RoomListSourceOfTruth<K, T>, key: K, entity: List<T>) {
        // Collects all keys from API call
        val inputKeys = entity.mapTo(HashSet()) { i -> keyBuilder(i) }
        val cached = sourceOfTruth.get(key)
        cached.forEach { storedEntity ->
            // if the store contains an entity not in the API response, it gets deleted
            if (!inputKeys.contains(keyBuilder(storedEntity))) {
                sourceOfTruth.delete(storedEntity)
            }
        }
    }
}

