package dev.pablodiste.datastore.coroutines

import kotlinx.coroutines.*

interface CoroutineConfig {
    val mainDispatcher: CoroutineDispatcher
    val ioDispatcher: CoroutineDispatcher
    val compDispatcher: CoroutineDispatcher
    val job: Job
    val applicationScope: CoroutineScope
}

class DefaultCoroutineConfig(
    override val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    override val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    override val compDispatcher: CoroutineDispatcher = Dispatchers.Default,
    override val job: Job = SupervisorJob(),
    override val applicationScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)
): CoroutineConfig