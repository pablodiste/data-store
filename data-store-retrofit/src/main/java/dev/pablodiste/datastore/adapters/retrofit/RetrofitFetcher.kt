package dev.pablodiste.datastore.adapters.retrofit

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.FetcherServiceProvider
import dev.pablodiste.datastore.StoreConfig.throttlingDetectedExceptions
import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.fetchers.joinInProgressCalls
import dev.pablodiste.datastore.fetchers.limit
import dev.pablodiste.datastore.fetchers.retry
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import dev.pablodiste.datastore.retry.RetryPolicy
import retrofit2.HttpException
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class RetrofitFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
    ) : Fetcher<K, I> {

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
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds, eventCount = 1),
            retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry,
            noinline fetch: suspend (K, S) -> I,
        ): Fetcher<K, I> {
            return object: RetrofitFetcher<K, I, S>(S::class.java, serviceProvider) {
                    override suspend fun fetch(key: K, service: S): I = fetch(key, service)
                }
                .joinInProgressCalls()
                .limit(rateLimitPolicy)
                .retry(retryPolicy)
        }
    }
}

/*
/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class RetrofitCrudFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds),
    override val retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry
): RetrofitFetcher<K, I, S>(serviceClass, serviceProvider, rateLimitPolicy, retryPolicy), CrudFetcher<K, I> {

    abstract suspend fun create(key: K, entity: I, service: S): FetcherResult<I>
    abstract suspend fun update(key: K, entity: I, service: S): FetcherResult<I>
    abstract suspend fun delete(key: K, entity: I, service: S): FetcherResult<I>

    override suspend fun create(key: K, entity: I): FetcherResult<I> = create(key, entity, service)
    override suspend fun update(key: K, entity: I): FetcherResult<I> = update(key, entity, service)
    override suspend fun delete(key: K, entity: I): FetcherResult<I> = delete(key, entity, service)
}
 */