package dev.pablodiste.datastore.adapters.retrofit

import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.StoreConfig.throttlingDetectedExceptions
import dev.pablodiste.datastore.impl.LimitedFetcher
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

interface RetrofitServiceProvider {
    fun <T> createService(service: Class<T>): T
}

/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class RetrofitFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: RetrofitServiceProvider,
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
    )
    : LimitedFetcher<K, I>(rateLimitPolicy) {

    init {
        throttlingDetectedExceptions.add(HttpException::class.java)
    }

    protected val service = serviceProvider.createService(serviceClass)

    abstract suspend fun fetch(key: K, service: S): FetcherResult<I>

    override suspend fun fetch(key: K): FetcherResult<I> = fetch(key, service)

    companion object {
        fun <K: Any, I: Any, S: Any> of(
            serviceClass: Class<S>,
            serviceProvider: RetrofitServiceProvider,
            fetch: suspend (K, S) -> FetcherResult<I>,
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
        ): Fetcher<K, I> {
            return object: RetrofitFetcher<K, I, S>(serviceClass, serviceProvider, rateLimitPolicy) {
                override suspend fun fetch(key: K, service: S): FetcherResult<I> = fetch(key, service)
            }
        }
    }

}


/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class RetrofitCrudFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: RetrofitServiceProvider,
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy(5, TimeUnit.SECONDS)
): RetrofitFetcher<K, I, S>(serviceClass, serviceProvider, rateLimitPolicy), CrudFetcher<K, I> {

    abstract suspend fun create(key: K, entity: I, service: S): FetcherResult<I>
    abstract suspend fun update(key: K, entity: I, service: S): FetcherResult<I>
    abstract suspend fun delete(key: K, entity: I, service: S): Boolean

    override suspend fun fetch(key: K): FetcherResult<I> = fetch(key, service)
    override suspend fun create(key: K, entity: I): FetcherResult<I> = create(key, entity, service)
    override suspend fun update(key: K, entity: I): FetcherResult<I> = update(key, entity, service)
    override suspend fun delete(key: K, entity: I): Boolean = delete(key, entity, service)

}