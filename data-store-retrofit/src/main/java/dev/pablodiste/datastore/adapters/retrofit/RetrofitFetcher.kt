package dev.pablodiste.datastore.adapters.retrofit

import dev.pablodiste.datastore.CrudFetcher
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.FetcherServiceProvider
import dev.pablodiste.datastore.StoreConfig.throttlingDetectedExceptions
import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.impl.LimitedFetcher
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import retrofit2.HttpException
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class RetrofitFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds))
    : LimitedFetcher<K, I>(rateLimitPolicy) {

    init {
        throttlingDetectedExceptions.add(HttpException::class.java)
    }

    protected val service = serviceProvider.createService(serviceClass)

    abstract suspend fun fetch(key: K, service: S): I

    override suspend fun fetch(key: K): FetcherResult<I> =
        try {
            FetcherResult.Data(fetch(key, service))
        } catch (ex: IOException) {
            FetcherResult.Error(FetcherError.IOError(ex))
        } catch (ex: HttpException) {
            FetcherResult.Error(FetcherError.HttpError(exception = ex, code = ex.code(), message = ex.message()))
        } catch (ex: Exception) {
            FetcherResult.Error(FetcherError.UnknownError(ex))
        }

    companion object {
        inline fun <K: Any, I: Any, reified S: Any> of(
            serviceProvider: FetcherServiceProvider,
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds),
            noinline fetch: suspend (K, S) -> I,
        ): Fetcher<K, I> {
            return object: RetrofitFetcher<K, I, S>(S::class.java, serviceProvider, rateLimitPolicy) {
                override suspend fun fetch(key: K, service: S): I = fetch(key, service)
            }
        }
    }
}


/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class RetrofitCrudFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds)
): RetrofitFetcher<K, I, S>(serviceClass, serviceProvider, rateLimitPolicy), CrudFetcher<K, I> {

    abstract suspend fun create(key: K, entity: I, service: S): FetcherResult<I>
    abstract suspend fun update(key: K, entity: I, service: S): FetcherResult<I>
    abstract suspend fun delete(key: K, entity: I, service: S): FetcherResult<I>

    override suspend fun create(key: K, entity: I): FetcherResult<I> = create(key, entity, service)
    override suspend fun update(key: K, entity: I): FetcherResult<I> = update(key, entity, service)
    override suspend fun delete(key: K, entity: I): FetcherResult<I> = delete(key, entity, service)
}