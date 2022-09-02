package dev.pablodiste.datastore.inmemory

import android.util.Log
import dev.pablodiste.datastore.SourceOfTruth
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class InMemoryListSourceOfTruth<K: Any, T: Any>: SourceOfTruth<K, List<T>> {

    private val data: MutableList<T> = mutableListOf()
    private val internalFlow: MutableSharedFlow<List<T>> = MutableSharedFlow(replay = 1,
        onBufferOverflow = BufferOverflow.SUSPEND,
        extraBufferCapacity = 0)

    abstract fun predicate(key: K): (key: K, value: T) -> Boolean

    override suspend fun exists(key: K): Boolean = data.any { predicate(key)(key, it) }

    override fun listen(key: K): Flow<List<T>> = internalFlow

    override suspend fun store(key: K, entity: List<T>, removeStale: Boolean): List<T> {
        data.addAll(entity)
        emit(key)
        return entity
    }

    override suspend fun get(key: K): List<T> = data.filter { predicate(key)(key, it) }

    override suspend fun delete(key: K): Boolean {
        val removed = data.removeAll { predicate(key)(key, it) }
        emit(key)
        return removed
    }

    private fun emit(key: K) {
        Log.d("SOT", "Emitting changes")
        val dataFound = data.filter { predicate(key)(key, it) }
        internalFlow.tryEmit(dataFound)
    }
}