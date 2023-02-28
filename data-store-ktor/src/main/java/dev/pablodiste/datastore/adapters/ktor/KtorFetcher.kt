package dev.pablodiste.datastore.adapters.ktor

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.FetcherServiceProvider
import dev.pablodiste.datastore.Sender
import dev.pablodiste.datastore.StoreConfig
import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.fetchers.joinInProgressCalls
import dev.pablodiste.datastore.fetchers.limit
import dev.pablodiste.datastore.fetchers.retry
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import dev.pablodiste.datastore.retry.RetryPolicy
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.utils.io.errors.IOException
import kotlin.time.Duration.Companion.seconds

abstract class KtorServiceCall<K: Any, I: Any, S: Any>(val service: S) {

    init {
        StoreConfig.throttlingDetectedExceptions.addAll(listOf(
            RedirectResponseException::class.java,
            ClientRequestException::class.java,
            ServerResponseException::class.java)
        )
    }

    suspend fun <I: Any> ktorFetch(operation: suspend () -> FetcherResult<I>) = try {
        operation.invoke()
    } catch (e: ClientRequestException) {
        FetcherResult.Error(FetcherError.HttpError(exception = e, code = e.response.status.value, message = e.message))
    } catch (e: ServerResponseException) {
        FetcherResult.Error(FetcherError.HttpError(exception = e, code = e.response.status.value, message = e.message))
    } catch (e: IOException) {
        FetcherResult.Error(FetcherError.IOError(e))
    } catch (e: Exception) {
        FetcherResult.Error(FetcherError.UnknownError(e))
    }
}

/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class KtorFetcher<K: Any, I: Any, S: Any>(service: S) :
    KtorServiceCall<K, I, S>(service), Fetcher<K, I> {

    abstract suspend fun fetch(key: K, service: S): I

    override suspend fun fetch(key: K): FetcherResult<I> = ktorFetch {
        FetcherResult.Data(fetch(key, service))
    }

    companion object {
        fun <K: Any, I: Any, S: Any> of(
            service: S,
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds, eventCount = 1),
            retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry,
            fetch: suspend (K, S) -> I,
        ): Fetcher<K, I> {
            return object: KtorFetcher<K, I, S>(service) {
                    override suspend fun fetch(key: K, service: S): I = fetch(key, service)
                }
                .joinInProgressCalls()
                .limit(rateLimitPolicy)
                .retry(retryPolicy)
        }
    }

}

abstract class KtorSender<K: Any, I: Any, S: Any>(service: S) :
    KtorServiceCall<K, I, S>(service), Sender<K, I> {

    abstract suspend fun send(key: K, entity: I, service: S): I

    override suspend fun send(key: K, entity: I): FetcherResult<I> = ktorFetch {
        FetcherResult.Data(
            send(key, entity, service)
        )
    }

    companion object {
        fun <K: Any, I: Any, S: Any> of(service: S, send: suspend (K, I, S) -> I): Sender<K, I> {
            return object: KtorSender<K, I, S>(service) {
                override suspend fun send(key: K, entity: I, service: S): I =
                    send(key, entity, service)
            }
        }

        fun <K: Any, I: Any, S: Any> noResult(service: S, send: suspend (K, I, S) -> Unit): Sender<K, I> {
            return object: KtorSender<K, I, S>(service) {
                override suspend fun send(key: K, entity: I): FetcherResult<I> =
                    ktorFetch {
                        send.invoke(key, entity, service)
                        FetcherResult.Success(success = true)
                    }

                override suspend fun send(key: K, entity: I, service: S): I = entity
            }
        }
    }
}