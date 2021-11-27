package com.pablodiste.android.datastore.ratelimiter

import java.util.concurrent.TimeUnit

open class RateLimitPolicy(val timeout: Int, val timeUnit: TimeUnit)