package dev.pablodiste.datastore

fun <K: Any, T: Any> Store<K, T>.stream(key: K, refresh: Boolean = true) = stream(StoreRequest(key, refresh = refresh))

suspend fun <K: Any, T: Any> Store<K, T>.get(key: K) = get(StoreRequest(key, refresh = true))

suspend fun <K: Any, T: Any> Store<K, T>.fetch(key: K, forced: Boolean = true) = fetch(StoreRequest(key, refresh = forced))
