package dev.pablodiste.datastore.adapters.ktor

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.FetcherServiceProvider
import dev.pablodiste.datastore.StoreConfig
import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.impl.LimitedFetcher
import dev.pablodiste.datastore.ratelimiter.RateLimitPolicy
import dev.pablodiste.datastore.retry.RetryPolicy
import io.ktor.client.plugins.*
import io.ktor.utils.io.errors.*
import kotlin.time.Duration.Companion.seconds

/**
 * Implements a retrofit service call, K = key, I: entity DTO class, S: Retrofit service class
 */
abstract class KtorFetcher<K: Any, I: Any, S: Any>(
    serviceClass: Class<S>,
    serviceProvider: FetcherServiceProvider,
    override val rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds),
    override val retryPolicy: RetryPolicy = RetryPolicy.DoNotRetry
) : LimitedFetcher<K, I>(rateLimitPolicy) {

    init {
        StoreConfig.throttlingDetectedExceptions.addAll(listOf(
            RedirectResponseException::class.java,
            ClientRequestException::class.java,
            ServerResponseException::class.java)
        )
    }

    protected val service = serviceProvider.createService(serviceClass)

    abstract suspend fun fetch(key: K, service: S): I

    override suspend fun fetch(key: K): FetcherResult<I> {
        return try {
            FetcherResult.Data(fetch(key, service))
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

    companion object {
        inline fun <K: Any, I: Any, reified S: Any> of(
            serviceProvider: FetcherServiceProvider,
            rateLimitPolicy: RateLimitPolicy = RateLimitPolicy.FixedWindowPolicy(duration = 5.seconds),
            noinline fetch: suspend (K, S) -> I,
        ): Fetcher<K, I> {
            return object: KtorFetcher<K, I, S>(S::class.java, serviceProvider, rateLimitPolicy) {
                override suspend fun fetch(key: K, service: S): I = fetch(key, service)
            }
        }
    }

}
