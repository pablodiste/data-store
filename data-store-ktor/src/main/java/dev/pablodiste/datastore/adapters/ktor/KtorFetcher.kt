package dev.pablodiste.datastore.adapters.ktor

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.FetcherServiceProvider
import dev.pablodiste.datastore.StoreConfig
import dev.pablodiste.datastore.impl.LimitedFetcher
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import io.ktor.client.plugins.*
import java.util.concurrent.TimeUnit

/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class KtorFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
) : LimitedFetcher<K, I>(rateLimitPolicy) {

    init {
        StoreConfig.throttlingDetectedExceptions.addAll(listOf(
            RedirectResponseException::class.java,
            ClientRequestException::class.java,
            ServerResponseException::class.java)
        )
    }

    protected val service = serviceProvider.createService(serviceClass)

    abstract suspend fun fetch(key: K, service: S): FetcherResult<I>

    override suspend fun fetch(key: K): FetcherResult<I> = fetch(key, service)

    companion object {
        fun <K: Any, I: Any, S: Any> of(
            serviceClass: Class<S>,
            serviceProvider: FetcherServiceProvider,
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS),
            fetch: suspend (K, S) -> FetcherResult<I>,
        ): Fetcher<K, I> {
            return object: KtorFetcher<K, I, S>(serviceClass, serviceProvider, rateLimitPolicy) {
                override suspend fun fetch(key: K, service: S): FetcherResult<I> = fetch(key, service)
            }
        }
    }

}
