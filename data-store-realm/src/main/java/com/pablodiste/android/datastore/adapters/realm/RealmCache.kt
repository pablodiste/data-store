package com.pablodiste.android.datastore.adapters.realm

import com.pablodiste.android.datastore.impl.ClosableCache
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
    private val stalenessPolicy: StalenessPolicy<K, T> = DoNotExpireStalenessPolicy()
    ): ClosableCache<K, T>() {

    abstract fun query(key: K): (query: RealmQuery<T>) -> Unit

    open fun storeInRealm(key: K, bgRealm: Realm, entity: T) { bgRealm.copyToRealmOrUpdate(entity) }

    open fun deleteFromRealm(key: K, bgRealm: Realm) = deleteAll(klass, bgRealm, query(key))

    override suspend fun get(key: K): T = findFirst(klass, query(key), closeableResourceManager)

    override suspend fun exists(key: K): Boolean = exists(klass, query(key))

    override fun listen(key: K): Flow<T> = findFirstAsFlow(klass, query(key))

    override suspend fun store(key: K, entity: T, removeStale: Boolean): T {
        executeRealmTransactionAwait { bgRealm ->
            if (removeStale && stalenessPolicy !is DoNotExpireStalenessPolicy) removeStale(bgRealm, key, entity)
            storeInRealm(key, bgRealm, entity)
        }
        return get(key)
    }

    override suspend fun delete(key: K): Boolean = executeRealmTransactionAwait { bgRealm -> deleteFromRealm(key, bgRealm) }

    //fun <U> getAndRun(key: K, operation: (result: T?) -> U): U = findFirstManagedAndRun(klass, query(key), operation)

    private fun removeStale(bgRealm: Realm, key: K, entity: T) = stalenessPolicy.removeStaleEntityInRealm(bgRealm, this, key, entity)

}

/**
 * Simple cache where the fetcher entity is the same as the entity stored in the cache.
 * Useful when we parse jsons over the same entities that are stored in the cache.
 */
abstract class SimpleRealmCache<K: Any, T: RealmObject>(
    klass: Class<T>,
    stalenessPolicy: StalenessPolicy<K, T> = DoNotExpireStalenessPolicy()
): RealmCache<K, T>(klass, stalenessPolicy)

/**
 * Database cache based on Realm, lists version
 */
abstract class RealmListCache<K: Any, T: RealmObject>(
    val klass: Class<T>,
    private val stalenessPolicy: StalenessPolicy<K, T> = DoNotExpireStalenessPolicy()
    ): ClosableCache<K, List<T>>() {

    abstract fun query(key: K): (query: RealmQuery<T>) -> Unit

    open fun storeInRealm(key: K, bgRealm: Realm, entity: List<T>) { bgRealm.copyToRealmOrUpdate(entity) }

    open fun deleteFromRealm(key: K, bgRealm: Realm) = deleteAll(klass, bgRealm, query(key))

    override suspend fun get(key: K): List<T> = findAll(klass, query(key), closeableResourceManager)

    override suspend fun exists(key: K): Boolean = exists(klass, query(key))

    override fun listen(key: K): Flow<List<T>> = findAllAsFlow(klass, query(key))

    override suspend fun store(key: K, entity: List<T>, removeStale: Boolean): List<T> {
        executeRealmTransactionAwait { bgRealm ->
            if (removeStale && stalenessPolicy !is DoNotExpireStalenessPolicy) removeStale(bgRealm, key, entity)
            storeInRealm(key, bgRealm, entity)
        }
        return get(key)
    }

    override suspend fun delete(key: K): Boolean = executeRealmTransactionAwait { bgRealm -> deleteFromRealm(key, bgRealm) }

    //fun <U> getAndRun(key: K, operation: (result: List<T>) -> U): U = findAllManagedAndRun(klass, query(key), operation)

    private fun removeStale(bgRealm: Realm, key: K, entity: List<T>) = stalenessPolicy.removeStaleListInRealm(bgRealm, this, key, entity)
}


/**
 * Simple cache where the fetcher entity is the same as the entity stored in the cache. (list version)
 * Useful when we parse jsons over the same entities that are stored in the cache.
 */
abstract class SimpleRealmListCache<K: Any, T: RealmObject>(
    klass: Class<T>,
    stalenessPolicy: StalenessPolicy<K, T> = DoNotExpireStalenessPolicy()
): RealmListCache<K, T>(klass, stalenessPolicy)

