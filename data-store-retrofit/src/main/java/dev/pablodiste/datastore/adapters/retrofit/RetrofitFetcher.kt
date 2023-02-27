package dev.pablodiste.datastore.adapters.retrofit

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

abstract class RetrofitServiceCall<K: Any, I: Any, S: Any>(val service: S) {
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
abstract class RetrofitFetcher<K: Any, I: Any, S: Any>(service: S):
    RetrofitServiceCall<K, I, S>(service), Fetcher<K, I> {

    abstract suspend fun fetch(key: K, service: S): I

    override suspend fun fetch(key: K): FetcherResult<I> = retrofitFetch {
        FetcherResult.Data(fetch(key, service))
    }

    companion object {
        fun <K: Any, I: Any, S: Any> of(
            service: S,
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds, eventCount = 1),
            retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry,
            fetch: suspend (K, S) -> I,
        ): Fetcher<K, I> {
            return object: RetrofitFetcher<K, I, S>(service) {
                    override suspend fun fetch(key: K, service: S): I = fetch(key, service)
                }
                .joinInProgressCalls()
                .limit(rateLimitPolicy)
                .retry(retryPolicy)
        }
    }
}

abstract class RetrofitSender<K: Any, I: Any, S: Any>(service: S) :
    RetrofitServiceCall<K, I, S>(service), Sender<K, I> {

    abstract suspend fun send(key: K, entity: I, service: S): I

    override suspend fun send(key: K, entity: I): FetcherResult<I> = retrofitFetch {
        FetcherResult.Data(
            send(key, entity, service)
        )
    }

    companion object {
        fun <K: Any, I: Any, S: Any> of(service: S, send: suspend (K, I, S) -> I): Sender<K, I> {
            return object: RetrofitSender<K, I, S>(service) {
                override suspend fun send(key: K, entity: I, service: S): I =
                    send(key, entity, service)
            }
        }

        fun <K: Any, I: Any, S: Any> noResult(service: S, send: suspend (K, I, S) -> Unit): Sender<K, I> {
            return object: RetrofitSender<K, I, S>(service) {
                override suspend fun send(key: K, entity: I): FetcherResult<I> =
                    retrofitFetch {
                        send.invoke(key, entity, service)
                        FetcherResult.Success(success = true)
                    }

                override suspend fun send(key: K, entity: I, service: S): I = entity
            }
        }

    }
}
