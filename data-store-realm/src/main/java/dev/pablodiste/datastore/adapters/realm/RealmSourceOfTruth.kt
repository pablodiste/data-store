package dev.pablodiste.datastore.adapters.realm

import dev.pablodiste.datastore.closable.ClosableSourceOfTruth
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import kotlinx.coroutines.flow.Flow

/**
 * Database source of truth based on Realm.
 * K: Class used for a key, used by the source of truth to find entities. For example: TeamDataStore.Key(teamId)
 * T: Class of the RealmObject to be stored. For example: Team : RealmObject
 */
abstract class RealmSourceOfTruth<K: Any, T: RealmObject>(
    val klass: Class<T>,
    private val stalenessPolicy: StalenessPolicy<K, T> = DoNotExpireStalenessPolicy()
    ): ClosableSourceOfTruth<K, T>() {

    abstract fun query(key: K): (query: RealmQuery<T>) -> Unit

    open fun storeInRealm(key: K, bgRealm: Realm, entity: T) { bgRealm.copyToRealmOrUpdate(entity) }

    open fun deleteFromRealm(key: K, bgRealm: Realm) =
        deleteAll(klass, bgRealm, query(key))

    override suspend fun get(key: K): T =
        findFirst(klass, query(key), closeableResourceManager)

    override suspend fun exists(key: K): Boolean =
        exists(klass, query(key))

    override fun listen(key: K): Flow<T> =
        findFirstAsFlow(klass, query(key), closeableResourceManager)

    override suspend fun store(key: K, entity: T, removeStale: Boolean): T {
        executeRealmTransactionAwait { bgRealm ->
            if (removeStale && stalenessPolicy !is DoNotExpireStalenessPolicy) removeStale(
                bgRealm,
                key,
                entity
            )
            storeInRealm(key, bgRealm, entity)
        }
        return get(key)
    }

    override suspend fun delete(key: K): Boolean =
        executeRealmTransactionAwait { bgRealm ->
            deleteFromRealm(
                key,
                bgRealm
            )
        }

    //fun <U> getAndRun(key: K, operation: (result: T?) -> U): U = findFirstManagedAndRun(klass, query(key), operation)

    private fun removeStale(bgRealm: Realm, key: K, entity: T) = stalenessPolicy.removeStaleEntityInRealm(bgRealm, this, key, entity)

}

