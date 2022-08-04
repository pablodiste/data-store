package com.pablodiste.android.datastore

import com.pablodiste.android.datastore.throttling.ThrottlingConfiguration
import com.pablodiste.android.datastore.throttling.ThrottlingFetcherController
import com.pablodiste.android.datastore.throttling.ThrottlingFetcherControllerImpl
import java.io.IOException

object StoreConfig {
    var isRateLimiterEnabled: () -> Boolean = { true }
    var isThrottlingEnabled: () -> Boolean = { true }
    var throttlingController: ThrottlingFetcherController = ThrottlingFetcherControllerImpl()
    var throttlingConfiguration: ThrottlingConfiguration = ThrottlingConfiguration()
    var throttlingDetectedExceptions: MutableSet<Class<out Exception>> = mutableSetOf(IOException::class.java)
}