package dev.pablodiste.datastore.inmemory

import android.util.Log
import dev.pablodiste.datastore.SourceOfTruth
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class InMemorySourceOfTruth<K: Any, T: Any>: SourceOfTruth<K, T> {

    private val data: MutableList<T> = mutableListOf()
    private val internalFlow: MutableSharedFlow<T> = MutableSharedFlow(replay = 1,
        onBufferOverflow = BufferOverflow.SUSPEND,
        extraBufferCapacity = 0)

    abstract fun predicate(key: K): (key: K, value: T) -> Boolean

    override suspend fun exists(key: K): Boolean = data.any { predicate(key)(key, it) }

    override fun listen(key: K): Flow<T> = internalFlow

    override suspend fun store(key: K, entity: T, removeStale: Boolean): T {
        data.add(entity)
        emit(key)
        return entity
    }

    override suspend fun get(key: K): T = data.find { predicate(key)(key, it) } ?:
        throw NoSuchElementException("Entity not found")

    override suspend fun delete(key: K): Boolean {
        val removed = data.removeAll { predicate(key)(key, it) }
        emit(key)
        return removed
    }

    private fun emit(key: K) {
        Log.d("SOT", "Emitting changes")
        val dataFound = data.find { predicate(key)(key, it) }
        if (dataFound != null) internalFlow.tryEmit(dataFound)
    }
}