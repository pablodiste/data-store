package com.pablodiste.android.datastore

import com.pablodiste.android.datastore.throttling.ThrottlingFetcherController
import com.pablodiste.android.datastore.throttling.ThrottlingFetcherControllerImpl

object StoreConfig {
    var isRateLimiterEnabled: () -> Boolean = { true }
    var isThrottlingEnabled: () -> Boolean = { true }
    var throttlingController: ThrottlingFetcherController = ThrottlingFetcherControllerImpl()
}