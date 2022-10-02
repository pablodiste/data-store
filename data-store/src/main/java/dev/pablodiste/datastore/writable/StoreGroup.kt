package dev.pablodiste.datastore.writable

import dev.pablodiste.datastore.writable.WorkerManager.getWorker


interface GroupableStore<T: Any> {
    var group: EntityStoreGroup<T>?
}

class EntityStoreGroup<T: Any>(val clazz: Class<T>) {

    fun <K: Any, I: Any> applyPendingChanges(key: K, entity: T): PendingChangesApplicationResult<T> =
        getWorker<K, T, I>(clazz)?.applyPendingChanges(key, entity) ?: PendingChangesApplicationResult(entity, true)

}

/**
 * Connects a list of stores that uses the same entity T.
 * This allows reapplying pending changes (writes) over fetched entities,
 * to maintain consistency between them and respect the local changes over the fetched ones.
 */
inline fun <reified T: Any> groupStoresByEntity(vararg stores: GroupableStore<T>) {
    if (stores.isNotEmpty()) {
        val entityGroup = EntityStoreGroup(T::class.java)
        stores.forEach {
            it.tryCast<GroupableStore<T>> {
                group = entityGroup
            }
        }
    }
}

inline fun <reified T> Any?.tryCast(block: T.() -> Unit) { if (this is T) { block() } }
