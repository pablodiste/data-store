package com.pablodiste.android.datastore.adapters.room

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.pablodiste.android.datastore.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Database cache based on Room (DAO), list version
 */
abstract class RoomCache<K: Any, T: Any>(
    private val tableName: String,
    private val database: RoomDatabase,
    private val stalenessPolicy: StalenessPolicy<K, T> = DoNotExpireStalenessPolicy()
): Cache<K, T> {

    override suspend fun get(key: K): T {
        val query = baseSelectQuery(key)
        val result = getEntitySync(query)
        return result ?: throw NoSuchElementException("Expected at least one element")
    }

    override suspend fun exists(key: K): Boolean {
        val where = where(key)
        val query = SimpleSQLiteQuery("SELECT EXISTS(SELECT * FROM $tableName $where)")
        return getBooleanSync(query)
    }

    override fun listen(key: K): Flow<T> = flow {
        coroutineScope {
            val query = baseSelectQuery(key)
            val observerChannel = Channel<Unit>(Channel.CONFLATED)
            val observer = object : InvalidationTracker.Observer(tableName) {
                override fun onInvalidated(tables: MutableSet<String>) {
                    observerChannel.trySend(Unit)
                }
            }
            observerChannel.trySend(Unit) // Initial signal to perform first query.
            val resultChannel = Channel<T>()
            launch {
                database.invalidationTracker.addObserver(observer)
                try {
                    // Iterate until cancelled, transforming observer signals to query results
                    // to be emitted to the flow.
                    for (signal in observerChannel) {
                        val result = getEntitySync(query)
                        result?.let { resultChannel.send(it) }
                    }
                } finally {
                    database.invalidationTracker.removeObserver(observer)
                }
            }
            emitAll(resultChannel)
        }
    }

    override suspend fun store(key: K, entity: T, removeStale: Boolean): T {
        withContext(Dispatchers.IO) {
            database.runInTransaction {
//                if (removeStale && stalenessPolicy !is DoNotExpireStalenessPolicy) removeStale(bgRealm, key, entity)
                upsert(entity)
            }
        }
        return get(key)
    }

    override suspend fun delete(key: K): Boolean {
        withContext(Dispatchers.IO) {
            val query = baseDeleteQuery(key)
            delete(query)
        }
        return true
    }

    private fun baseSelectQuery(key: K): SimpleSQLiteQuery {
        val where = where(key)
        return SimpleSQLiteQuery("SELECT * FROM $tableName $where")
    }

    private fun baseDeleteQuery(key: K): SimpleSQLiteQuery {
        val where = where(key)
        return SimpleSQLiteQuery("DELETE FROM $tableName $where")
    }

    private fun where(key: K): String {
        val query = query(key)
        return if (query.isEmpty()) "" else "WHERE $query"
    }

    abstract fun query(key: K): String

    @RawQuery
    protected abstract suspend fun getEntitySync(query: SupportSQLiteQuery): T?

    @RawQuery
    protected abstract suspend fun getBooleanSync(query: SupportSQLiteQuery): Boolean

    @RawQuery
    protected abstract suspend fun delete(query: SupportSQLiteQuery): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun upsert(entity: T)

    // fun <U> getAndRun(key: K, operation: (result: List<T>) -> U): U = findAllManagedAndRun(klass, query(key), operation)

    // private fun removeStale(bgRealm: Realm, key: K, entity: List<T>) = stalenessPolicy.removeStaleListInRealm(bgRealm, this, key, entity)

}

/*
abstract class SimpleRoomCache<K: Any, T: Any>(
    private val tableName: String,
    private val database: RoomDatabase,
    stalenessPolicy: StalenessPolicy<K, T> = DoNotExpireStalenessPolicy()
): RoomCache<K, T>(tableName, database, stalenessPolicy)
 */
