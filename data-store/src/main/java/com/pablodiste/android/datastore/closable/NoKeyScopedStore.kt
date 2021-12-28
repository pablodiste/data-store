package com.pablodiste.android.datastore.closable

import com.pablodiste.android.datastore.StoreResponse
import com.pablodiste.android.datastore.impl.NoKey
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.typeOf

typealias NoKeyScopedSimpleStore<T> = ScopedSimpleStoreImpl<NoKey, T>

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T: Any> NoKeyScopedSimpleStore<T>.stream(refresh: Boolean): Flow<StoreResponse<T>> = stream(NoKey( typeOf<T>().toString() ), refresh)
@OptIn(ExperimentalStdlibApi::class)
suspend inline fun <reified T: Any> NoKeyScopedSimpleStore<T>.get(): StoreResponse<T> = get(NoKey( typeOf<T>().toString() ))
@OptIn(ExperimentalStdlibApi::class)
suspend inline fun <reified T: Any> NoKeyScopedSimpleStore<T>.fetch(forced: Boolean): StoreResponse<T> = fetch(NoKey( typeOf<T>().toString() ), forced)
@OptIn(ExperimentalStdlibApi::class)
suspend inline fun <reified T: Any> NoKeyScopedSimpleStore<T>.performFetch(forced: Boolean): StoreResponse<T> = performFetch(NoKey( typeOf<T>().toString() ), forced)
