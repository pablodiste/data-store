package dev.pablodiste.datastore.adapters.ktor

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.FetcherServiceProvider
import dev.pablodiste.datastore.StoreConfig
import dev.pablodiste.datastore.impl.LimitedFetcher
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import io.ktor.client.plugins.*
import kotlin.time.Duration.Companion.seconds

/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class KtorFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds)
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
        inline fun <K: Any, I: Any, reified S: Any> of(
            serviceProvider: FetcherServiceProvider,
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds),
            noinline fetch: suspend (K, S) -> FetcherResult<I>,
        ): Fetcher<K, I> {
            return object: KtorFetcher<K, I, S>(S::class.java, serviceProvider, rateLimitPolicy) {
                override suspend fun fetch(key: K, service: S): FetcherResult<I> = fetch(key, service)
            }
        }
    }

}
