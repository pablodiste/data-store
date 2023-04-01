package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.StoreResponse
import dev.pablodiste.datastore.fetch
import dev.pablodiste.datastore.get
import dev.pablodiste.datastore.stream
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.typeOf

data class NoKey(val className: String = "")

typealias NoKeySimpleStore<T> = SimpleStoreImpl<NoKey, T>

inline fun <reified T: Any> NoKeySimpleStore<T>.stream(refresh: Boolean): Flow<StoreResponse<T>> = stream(NoKey( typeOf<T>().toString() ), refresh)
suspend inline fun <reified T: Any> NoKeySimpleStore<T>.get(): StoreResponse<T> = get(NoKey( typeOf<T>().toString() ))
suspend inline fun <reified T: Any> NoKeySimpleStore<T>.fetch(forced: Boolean = false): StoreResponse<T> = fetch(NoKey( typeOf<T>().toString() ), forced)
suspend inline fun <reified T: Any> NoKeySimpleStore<T>.performFetch(forced: Boolean = false): StoreResponse<T> = performFetch(NoKey( typeOf<T>().toString() ), forced)
