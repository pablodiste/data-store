package dev.pablodiste.datastore.adapters.ktor

import dev.pablodiste.datastore.ChangeOperation
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

abstract class KtorServiceCall<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider
) {

    val service = serviceProvider.createService(serviceClass)

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
abstract class KtorFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
) : KtorServiceCall<K, I, S>(serviceClass, serviceProvider), Fetcher<K, I> {

    abstract suspend fun fetch(key: K, service: S): I

    override suspend fun fetch(key: K): FetcherResult<I> = ktorFetch {
        FetcherResult.Data(fetch(key, service))
    }

    companion object {
        inline fun <K: Any, I: Any, reified S: Any> of(
            serviceProvider: FetcherServiceProvider,
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds, eventCount = 1),
            retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry,
            noinline fetch: suspend (K, S) -> I,
        ): Fetcher<K, I> {
            return object: KtorFetcher<K, I, S>(S::class.java, serviceProvider) {
                    override suspend fun fetch(key: K, service: S): I = fetch(key, service)
                }
                .joinInProgressCalls()
                .limit(rateLimitPolicy)
                .retry(retryPolicy)
        }
    }

}

abstract class KtorSender<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
) : KtorServiceCall<K, I, S>(serviceClass, serviceProvider), Sender<K, I> {

    abstract suspend fun send(key: K, entity: I, changeOperation: ChangeOperation, service: S): I

    override suspend fun send(key: K, entity: I, changeOperation: ChangeOperation): FetcherResult<I> = ktorFetch {
        FetcherResult.Data(
            send(key, entity, changeOperation, service)
        )
    }

    companion object {
        inline fun <K: Any, I: Any, reified S: Any> of(
            serviceProvider: FetcherServiceProvider,
            noinline send: suspend (K, I, S, ChangeOperation) -> I,
        ): Sender<K, I> {
            return object: KtorSender<K, I, S>(S::class.java, serviceProvider) {
                override suspend fun send(key: K, entity: I, changeOperation: ChangeOperation, service: S): I =
                    send(key, entity, service, changeOperation)
            }
        }

        inline fun <K: Any, I: Any, reified S: Any> noResult(
            serviceProvider: FetcherServiceProvider,
            noinline send: suspend (K, I, S, ChangeOperation) -> Unit,
        ): Sender<K, I> {
            return object: KtorSender<K, I, S>(S::class.java, serviceProvider) {
                override suspend fun send(key: K, entity: I, changeOperation: ChangeOperation): FetcherResult<I> =
                    ktorFetch {
                        send.invoke(key, entity, service, changeOperation)
                        FetcherResult.Success(success = true)
                    }

                override suspend fun send(key: K, entity: I, changeOperation: ChangeOperation, service: S): I = entity
            }
        }
    }
}