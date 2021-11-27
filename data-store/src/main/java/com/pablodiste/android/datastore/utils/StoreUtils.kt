package com.pablodiste.android.datastore.utils

import com.pablodiste.android.datastore.Store
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
