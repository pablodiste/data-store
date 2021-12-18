package com.pablodiste.android.datastore.impl

import com.pablodiste.android.datastore.StoreResponse
import kotlinx.coroutines.flow.Flow

data class NoKey(val value: Int = 0)

typealias NoKeySimpleStore<T> = SimpleStoreImpl<NoKey, T>

fun <T: Any> NoKeySimpleStore<T>.stream(refresh: Boolean): Flow<StoreResponse<T>> = stream(NoKey(), refresh)
suspend fun <T: Any> NoKeySimpleStore<T>.get(): StoreResponse<T> = get(NoKey())
suspend fun <T: Any> NoKeySimpleStore<T>.fetch(forced: Boolean): StoreResponse<T> = fetch(NoKey(), forced)
suspend fun <T: Any> NoKeySimpleStore<T>.performFetch(forced: Boolean): StoreResponse<T> = performFetch(NoKey(), forced)
