package dev.pablodiste.datastore.inmemory

import android.util.Log
import dev.pablodiste.datastore.SourceOfTruth
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.mapNotNull

abstract class InMemorySourceOfTruth<K: Any, T: Any>: SourceOfTruth<K, T> {

    private val data: MutableList<T> = mutableListOf()
    private val internalFlow: MutableSharedFlow<Boolean> = MutableSharedFlow(replay = 1,
        onBufferOverflow = BufferOverflow.SUSPEND,
        extraBufferCapacity = 0)

    abstract fun predicate(key: K): (key: K, value: T) -> Boolean

    override suspend fun exists(key: K): Boolean = data.any { predicate(key)(key, it) }

    override fun listen(key: K): Flow<T> = internalFlow.mapNotNull { data.firstOrNull { predicate(key)(key, it) } }

    override suspend fun store(key: K, entity: T, removeStale: Boolean): T {
        data.add(entity)
        emit()
        return entity
    }

    override suspend fun get(key: K): T = data.find { predicate(key)(key, it) } ?:
        throw NoSuchElementException("Entity not found")

    override suspend fun delete(key: K): Boolean {
        val removed = data.removeAll { predicate(key)(key, it) }
        emit()
        return removed
    }

    private fun emit() {
        Log.d("SOT", "Emitting changes")
        if (data.isNotEmpty()) internalFlow.tryEmit(true)
    }
}