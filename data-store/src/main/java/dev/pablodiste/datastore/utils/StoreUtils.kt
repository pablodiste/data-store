package dev.pablodiste.datastore.utils

import dev.pablodiste.datastore.Store
import dev.pablodiste.datastore.fetch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

fun <K: Any, T: Any> Store<K, T>.fetchAndForget(key: K) {
    val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    coroutineScope.launch {
        fetch(key)
    }
}
