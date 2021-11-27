package com.pablodiste.android.datastore.adapters.realm

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.toFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlin.coroutines.coroutineContext

/**
 * Finds a single RealmObject in Realm searching by the runQuery specified.
 * @param javaClass Class of the RealmObject to search for
 * @param runQuery query to execute, you can add filters and ordering here
 * @return the managed object found
 */
suspend fun <T : RealmObject> findFirst(javaClass: Class<T>, runQuery: (query: RealmQuery<T>) -> Unit = {}): T {
    val realm = Realm.getDefaultInstance()
    val baseQuery = realm.where(javaClass)
    runQuery(baseQuery)
    coroutineContext[Job]?.invokeOnCompletion {
        realm.close()
    }
    return baseQuery.findFirstAsync()
        .toFlow()
        .filter { it?.let { result -> result.isLoaded && result.isValid } ?: false }
        .first()!!
}

/**
 * Finds a group of RealmObjects filtering by the specified query.
 * @param javaClass Class of the RealmObject to search for
 * @param runQuery query to execute, you can add filters and ordering here
 * @return the managed object found
 */
suspend fun <T : RealmObject> findAll(javaClass: Class<T>, runQuery: (query: RealmQuery<T>) -> Unit = {}): List<T> {
    val realm = Realm.getDefaultInstance()
    val baseQuery = realm.where(javaClass)
    runQuery(baseQuery)
    coroutineContext[Job]?.invokeOnCompletion {
        realm.close()
    }
    return baseQuery.findAllAsync().toFlow()
        .filter { !realm.isClosed && it.isLoaded }
        .first()
}

/**
 * Returns a Flow with a list of RealmObjects matching the query provided and keeps listening to changes.
 * @param javaClass Class of the RealmObject to search for
 * @param runQuery query to execute, you can add filters and ordering here
 * @return a Flow emitting a list of managed Realm objects -proxies-
 */
fun <T : RealmObject> findAllAsFlow(javaClass: Class<T>, runQuery: (query: RealmQuery<T>) -> Unit = {}): Flow<List<T>> {
    val realm = Realm.getDefaultInstance()
    val baseQuery = realm.where(javaClass)
    runQuery(baseQuery)
    return baseQuery.findAllAsync().toFlow()
        .filter { !realm.isClosed && it.isLoaded }
        .onCompletion {
            realm.close()
        }
}

/**
 * Returns a Flowable with a single RealmObject matching the query provided and keeps listening to changes.
 * @param javaClass Class of the RealmObject to search for
 * @param runQuery query to execute, you can add filters here
 * @return a Flowable emitting a Realm objects -proxy-
 */
fun <T : RealmObject> findFirstAsFlow(javaClass: Class<T>, runQuery: (query: RealmQuery<T>) -> Unit = {}): Flow<T> {
    return findAllAsFlow(javaClass, runQuery).filter { it.isNotEmpty() }.map { it.first() }
}

/**
 * Write to realm through an async transaction.
 * @return Single function that will return true on success. It makes sense for this to be a single
 * over a completable because this will most likely get flatMapped from a endpoint single call.
 */
suspend fun executeRealmTransactionAwait(transaction: (bgRealm: Realm) -> Unit): Boolean {
    val realm = Realm.getDefaultInstance()
    realm.executeTransactionAwait { bgRealm ->
        transaction.invoke(bgRealm)
    }
    realm.close()
    return true
}

/**
 * Returns true if exists at least one RealmObject matching the provided query
 * @param javaClass Class of the RealmObject to search for
 * @param runQuery query to execute, you can add filters here
 * @return true if exists at least one matching object, otherwise false
 */
inline fun <T: RealmObject> exists(javaClass: Class<T>, runQuery: (query: RealmQuery<T>) -> Unit = {}): Boolean {
    val realm = Realm.getDefaultInstance()
    val baseQuery = realm.where(javaClass)
    runQuery(baseQuery)
    val exists = baseQuery.findFirst() != null
    realm.close()
    return exists
}

/**
 * Deletes realm objects based on the provided query
 * @param javaClass Class of the RealmObject to search for
 * @param runQuery query to execute, you can add filters here
 */
fun <T : RealmObject> deleteAll(javaClass: Class<T>, realm: Realm, runQuery: (query: RealmQuery<T>) -> Unit = {}) {
    val baseQuery = realm.where(javaClass)
    runQuery(baseQuery)
    baseQuery.findAll().deleteAllFromRealm()
}