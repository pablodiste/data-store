package dev.pablodiste.datastore

data class StoreRequest<K>(
    val key: K,
    val refresh: Boolean,
    val fetchWhenNoDataFound: Boolean = true)