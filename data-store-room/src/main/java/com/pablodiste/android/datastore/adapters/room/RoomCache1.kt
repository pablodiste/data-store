package com.pablodiste.android.datastore.adapters.room

/**
 * Database cache based on Room (DAO), list version
abstract class RoomListCache<K: Any, T: Any>(
    private val tableName: String,
    private val database: RoomDatabase,
    private val stalenessPolicy: StalenessPolicy<K, T> = DoNotExpireStalenessPolicy()
): ListCache<K, T> {

    override suspend fun get(key: K): List<T> {
        val where = query(key)
        val query = SimpleSQLiteQuery("SELECT * FROM $tableName WHERE $where")
        val result = getEntitySync(query)
        return result ?: throw NoSuchElementException("Expected at least one element")
    }

    override suspend fun exists(key: K): Boolean {
        val where = query(key)
        val query = SimpleSQLiteQuery("SELECT EXISTS(SELECT * FROM $tableName WHERE $where)")
        return getBooleanSync(query)
    }

    override fun listen(key: K): Flow<List<T>> = flow {
        coroutineScope {
            val where = query(key)
            val query = SimpleSQLiteQuery("SELECT * FROM $tableName WHERE $where")
            val observerChannel = Channel<Unit>(Channel.CONFLATED)
            val observer = object : InvalidationTracker.Observer(tableName) {
                override fun onInvalidated(tables: MutableSet<String>) {
                    observerChannel.trySend(Unit)
                }
            }
            observerChannel.trySend(Unit) // Initial signal to perform first query.
            val resultChannel = Channel<List<T>>()
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

    override suspend fun store(key: K, entity: List<T>, removeStale: Boolean): List<T> {
        /*
        executeRealmTransactionAwait { bgRealm ->
            if (removeStale && stalenessPolicy !is DoNotExpireStalenessPolicy) removeStale(bgRealm, key, entity)
            storeInRealm(key, bgRealm, entity)
        }
         */
        upsert(entity)
        return get(key)
    }

    override suspend fun delete(key: K): Boolean {
        /*
        val where = query(key)
        val query = SimpleSQLiteQuery("DELETE FROM $tableName WHERE $where")
        delete(query)

         */
        return true
    }

    @RawQuery
    protected abstract suspend fun getEntitySync(query: SupportSQLiteQuery): List<T>?

    @RawQuery
    protected abstract suspend fun getBooleanSync(query: SupportSQLiteQuery): Boolean

    //@RawQuery
    //protected abstract suspend fun delete(query: SupportSQLiteQuery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun upsert(entity: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun upsert(entities: List<T>)

    @Update
    abstract fun update(entity: T)

    @Update
    abstract fun update(entities: List<T>)

    @Delete
    abstract fun delete(entity: T)

    @Delete
    abstract fun delete(entities: List<T>)

    abstract fun query(key: K): String

    // fun <U> getAndRun(key: K, operation: (result: List<T>) -> U): U = findAllManagedAndRun(klass, query(key), operation)

    // private fun removeStale(bgRealm: Realm, key: K, entity: List<T>) = stalenessPolicy.removeStaleListInRealm(bgRealm, this, key, entity)

}

abstract class SimpleRoomListCache<K: Any, T: Any>(
    tableName: String,
    database: RoomDatabase,
    stalenessPolicy: StalenessPolicy<K, T> = DoNotExpireStalenessPolicy()
): RoomListCache<K, T>(tableName, database, stalenessPolicy)
 */
