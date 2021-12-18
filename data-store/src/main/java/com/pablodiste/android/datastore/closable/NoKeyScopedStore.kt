package com.pablodiste.android.datastore.closable

import com.pablodiste.android.datastore.StoreResponse
import com.pablodiste.android.datastore.impl.NoKey
import com.pablodiste.android.datastore.impl.ScopedSimpleStoreImpl
import kotlinx.coroutines.flow.Flow

typealias NoKeyScopedSimpleStore<T> = ScopedSimpleStoreImpl<NoKey, T>

fun <T: Any> NoKeyScopedSimpleStore<T>.stream(refresh: Boolean): Flow<StoreResponse<T>> = stream(NoKey(), refresh)
suspend fun <T: Any> NoKeyScopedSimpleStore<T>.get(): StoreResponse<T> = get(NoKey())
suspend fun <T: Any> NoKeyScopedSimpleStore<T>.fetch(forced: Boolean): StoreResponse<T> = fetch(NoKey(), forced)
suspend fun <T: Any> NoKeyScopedSimpleStore<T>.performFetch(forced: Boolean): StoreResponse<T> = performFetch(NoKey(), forced)
