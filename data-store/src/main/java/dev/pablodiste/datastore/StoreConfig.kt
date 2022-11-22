package dev.pablodiste.datastore

import dev.pablodiste.datastore.coroutines.CoroutineConfig
import dev.pablodiste.datastore.coroutines.DefaultCoroutineConfig
import dev.pablodiste.datastore.throttling.ThrottlingConfiguration
import dev.pablodiste.datastore.throttling.ThrottlingFetcherController
import dev.pablodiste.datastore.throttling.ThrottlingFetcherControllerImpl
import java.io.IOException

object StoreConfig {
    var coroutineConfig: CoroutineConfig = DefaultCoroutineConfig()
    var isRateLimiterEnabled: () -> Boolean = { true }
    var isThrottlingEnabled: () -> Boolean = { true }
    var throttlingController: ThrottlingFetcherController = ThrottlingFetcherControllerImpl()
    var throttlingConfiguration: ThrottlingConfiguration = ThrottlingConfiguration()
    var throttlingDetectedExceptions: MutableSet<Class<out Exception>> = mutableSetOf(IOException::class.java)
}