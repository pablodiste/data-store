package dev.pablodiste.datastore.adapters.retrofit

import dev.pablodiste.datastore.ChangeOperation
import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.FetcherServiceProvider
import dev.pablodiste.datastore.Sender
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

abstract class RetrofitServiceCall<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider
) {

    val service = serviceProvider.createService(serviceClass)

    init {
        throttlingDetectedExceptions.add(HttpException::class.java)
    }

    suspend fun <I: Any> retrofitFetch(operation: suspend () -> FetcherResult<I>) = try {
        operation.invoke()
    } catch (ex: IOException) {
        FetcherResult.Error(FetcherError.IOError(ex))
    } catch (ex: HttpException) {
        FetcherResult.Error(FetcherError.HttpError(exception = ex, code = ex.code(), message = ex.message()))
    } catch (ex: Exception) {
        FetcherResult.Error(FetcherError.UnknownError(ex))
    }
}

/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class RetrofitFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
    ) : RetrofitServiceCall<K, I, S>(serviceClass, serviceProvider), Fetcher<K, I> {

    abstract suspend fun fetch(key: K, service: S): I

    override suspend fun fetch(key: K): FetcherResult<I> = retrofitFetch {
        FetcherResult.Data(fetch(key, service))
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

abstract class RetrofitSender<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
) : RetrofitServiceCall<K, I, S>(serviceClass, serviceProvider), Sender<K, I> {

    abstract suspend fun send(key: K, entity: I, changeOperation: ChangeOperation, service: S): I

    override suspend fun send(key: K, entity: I, changeOperation: ChangeOperation): FetcherResult<I> = retrofitFetch {
        FetcherResult.Data(
            send(key, entity, changeOperation, service)
        )
    }

    companion object {
        inline fun <K: Any, I: Any, reified S: Any> of(
            serviceProvider: FetcherServiceProvider,
            noinline send: suspend (K, I, S, ChangeOperation) -> I,
        ): Sender<K, I> {
            return object: RetrofitSender<K, I, S>(S::class.java, serviceProvider) {
                override suspend fun send(key: K, entity: I, changeOperation: ChangeOperation, service: S): I =
                    send(key, entity, service, changeOperation)
            }
        }

        inline fun <K: Any, I: Any, reified S: Any> noResult(
            serviceProvider: FetcherServiceProvider,
            noinline send: suspend (K, I, S, ChangeOperation) -> Unit,
        ): Sender<K, I> {
            return object: RetrofitSender<K, I, S>(S::class.java, serviceProvider) {
                override suspend fun send(key: K, entity: I, changeOperation: ChangeOperation): FetcherResult<I> =
                    retrofitFetch {
                        send.invoke(key, entity, service, changeOperation)
                        FetcherResult.Success(success = true)
                    }

                override suspend fun send(key: K, entity: I, changeOperation: ChangeOperation, service: S): I = entity
            }
        }

    }
}
