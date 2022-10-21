package dev.pablodiste.datastore.retry

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface Retry {
    fun shouldRetry(): Boolean
}

class ExponentialBackoffRetry(val maxRetries: Int = 1,
                              val initialBackoff: Duration = 2.seconds,
                              val backoffRate: Int = 2): Retry {

    override fun shouldRetry(): Boolean {
        TODO("Not yet implemented")
    }
}