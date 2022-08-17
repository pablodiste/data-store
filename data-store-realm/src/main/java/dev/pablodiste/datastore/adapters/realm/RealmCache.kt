package dev.pablodiste.datastore.adapters.realm

import dev.pablodiste.datastore.closable.ClosableCache
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import kotlinx.coroutines.flow.Flow

/**
 * Database cache based on Realm.
 * K: Class used for a key, used by the cache to find entities. For example: TeamDataStore.Key(teamId)
 * T: Class of the RealmObject to be stored. For example: Team : RealmObject
 */
abstract class RealmCache<K: Any, T: RealmObject>(
    val klass: Class<T>,
    private val stalenessPolicy: dev.pablodiste.datastore.adapters.realm.StalenessPolicy<K, T> = dev.pablodiste.datastore.adapters.realm.DoNotExpireStalenessPolicy()
    ): ClosableCache<K, T>() {

    abstract fun query(key: K): (query: RealmQuery<T>) -> Unit

    open fun storeInRealm(key: K, bgRealm: Realm, entity: T) { bgRealm.copyToRealmOrUpdate(entity) }

    open fun deleteFromRealm(key: K, bgRealm: Realm) =
        dev.pablodiste.datastore.adapters.realm.deleteAll(klass, bgRealm, query(key))

    override suspend fun get(key: K): T =
        dev.pablodiste.datastore.adapters.realm.findFirst(klass, query(key), closeableResourceManager)

    override suspend fun exists(key: K): Boolean =
        dev.pablodiste.datastore.adapters.realm.exists(klass, query(key))

    override fun listen(key: K): Flow<T> =
        dev.pablodiste.datastore.adapters.realm.findFirstAsFlow(klass, query(key))

    override suspend fun store(key: K, entity: T, removeStale: Boolean): T {
        dev.pablodiste.datastore.adapters.realm.executeRealmTransactionAwait { bgRealm ->
            if (removeStale && stalenessPolicy !is dev.pablodiste.datastore.adapters.realm.DoNotExpireStalenessPolicy) removeStale(
                bgRealm,
                key,
                entity
            )
            storeInRealm(key, bgRealm, entity)
        }
        return get(key)
    }

    override suspend fun delete(key: K): Boolean =
        dev.pablodiste.datastore.adapters.realm.executeRealmTransactionAwait { bgRealm ->
            deleteFromRealm(
                key,
                bgRealm
            )
        }

    //fun <U> getAndRun(key: K, operation: (result: T?) -> U): U = findFirstManagedAndRun(klass, query(key), operation)

    private fun removeStale(bgRealm: Realm, key: K, entity: T) = stalenessPolicy.removeStaleEntityInRealm(bgRealm, this, key, entity)

}

/**
 * Simple cache where the fetcher entity is the same as the entity stored in the cache.
 * Useful when we parse jsons over the same entities that are stored in the cache.
 */
abstract class SimpleRealmCache<K: Any, T: RealmObject>(
    klass: Class<T>,
    stalenessPolicy: dev.pablodiste.datastore.adapters.realm.StalenessPolicy<K, T> = dev.pablodiste.datastore.adapters.realm.DoNotExpireStalenessPolicy()
): dev.pablodiste.datastore.adapters.realm.RealmCache<K, T>(klass, stalenessPolicy)

/**
 * Database cache based on Realm, lists version
 */
abstract class RealmListCache<K: Any, T: RealmObject>(
    val klass: Class<T>,
    private val stalenessPolicy: dev.pablodiste.datastore.adapters.realm.StalenessPolicy<K, T> = dev.pablodiste.datastore.adapters.realm.DoNotExpireStalenessPolicy()
    ): ClosableCache<K, List<T>>() {

    abstract fun query(key: K): (query: RealmQuery<T>) -> Unit

    open fun storeInRealm(key: K, bgRealm: Realm, entity: List<T>) { bgRealm.copyToRealmOrUpdate(entity) }

    open fun deleteFromRealm(key: K, bgRealm: Realm) =
        dev.pablodiste.datastore.adapters.realm.deleteAll(klass, bgRealm, query(key))

    override suspend fun get(key: K): List<T> =
        dev.pablodiste.datastore.adapters.realm.findAll(klass, query(key), closeableResourceManager)

    override suspend fun exists(key: K): Boolean =
        dev.pablodiste.datastore.adapters.realm.exists(klass, query(key))

    override fun listen(key: K): Flow<List<T>> =
        dev.pablodiste.datastore.adapters.realm.findAllAsFlow(klass, query(key))

    override suspend fun store(key: K, entity: List<T>, removeStale: Boolean): List<T> {
        dev.pablodiste.datastore.adapters.realm.executeRealmTransactionAwait { bgRealm ->
            if (removeStale && stalenessPolicy !is dev.pablodiste.datastore.adapters.realm.DoNotExpireStalenessPolicy) removeStale(
                bgRealm,
                key,
                entity
            )
            storeInRealm(key, bgRealm, entity)
        }
        return get(key)
    }

    override suspend fun delete(key: K): Boolean =
        dev.pablodiste.datastore.adapters.realm.executeRealmTransactionAwait { bgRealm ->
            deleteFromRealm(
                key,
                bgRealm
            )
        }

    //fun <U> getAndRun(key: K, operation: (result: List<T>) -> U): U = findAllManagedAndRun(klass, query(key), operation)

    private fun removeStale(bgRealm: Realm, key: K, entity: List<T>) = stalenessPolicy.removeStaleListInRealm(bgRealm, this, key, entity)
}


/**
 * Simple cache where the fetcher entity is the same as the entity stored in the cache. (list version)
 * Useful when we parse jsons over the same entities that are stored in the cache.
 */
abstract class SimpleRealmListCache<K: Any, T: RealmObject>(
    klass: Class<T>,
    stalenessPolicy: dev.pablodiste.datastore.adapters.realm.StalenessPolicy<K, T> = dev.pablodiste.datastore.adapters.realm.DoNotExpireStalenessPolicy()
): dev.pablodiste.datastore.adapters.realm.RealmListCache<K, T>(klass, stalenessPolicy)

