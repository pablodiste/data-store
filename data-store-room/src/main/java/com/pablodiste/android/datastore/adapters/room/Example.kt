package com.pablodiste.android.datastore.adapters.room

import kotlinx.coroutines.flow.Flow
import kotlin.collections.List

data class Entity(val name: String = "")

interface ExampleBaseInterface<T: Any> {
    fun foo(): Flow<T>
}

abstract class ExampleAbstract<T: Any>: ExampleBaseInterface<T> {
    override fun foo(): Flow<T> = TODO()
}

abstract class ExampleAbstractList<T: Any>: ExampleBaseInterface<List<T>> {
    override fun foo(): Flow<List<T>> = TODO()
}

class ExampleClassList: ExampleAbstractList<Entity>()


/*
interface ExampleListInterface<T: Any>: ExampleInterface<List<T>> {
    override fun foo(): Flow<List<T>>
}
*/
