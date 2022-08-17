package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.StoreResponse
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.typeOf

data class NoKey(val className: String = "")

typealias NoKeySimpleStore<T> = SimpleStoreImpl<NoKey, T>

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T: Any> NoKeySimpleStore<T>.stream(refresh: Boolean): Flow<StoreResponse<T>> = stream(NoKey( typeOf<T>().toString() ), refresh)
@OptIn(ExperimentalStdlibApi::class)
suspend inline fun <reified T: Any> NoKeySimpleStore<T>.get(): StoreResponse<T> = get(NoKey( typeOf<T>().toString() ))
@OptIn(ExperimentalStdlibApi::class)
suspend inline fun <reified T: Any> NoKeySimpleStore<T>.fetch(forced: Boolean): StoreResponse<T> = fetch(NoKey( typeOf<T>().toString() ), forced)
@OptIn(ExperimentalStdlibApi::class)
suspend inline fun <reified T: Any> NoKeySimpleStore<T>.performFetch(forced: Boolean): StoreResponse<T> = performFetch(NoKey( typeOf<T>().toString() ), forced)
