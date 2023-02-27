package dev.pablodiste.datastore.adapters.retrofit

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult
import dev.pablodiste.datastore.exceptions.FetcherError
import dev.pablodiste.datastore.fetchers.JoinInProgressCallFetcher
import retrofit2.HttpException
import retrofit2.Retrofit

class DeserializeErrorFetcher<K: Any, I: Any, E: Any>(val fetcher: Fetcher<K, I>, val retrofit: Retrofit, val clazz: Class<E>): Fetcher<K, I> {

    override suspend fun fetch(key: K): FetcherResult<I> {
        val response = fetcher.fetch(key)
        return if (
            response is FetcherResult.Error &&
            response.error is FetcherError.HttpError &&
            (response.error.exception as HttpException).response()?.errorBody() != null) {
            val error = response.error as FetcherError.HttpError
            val converter = retrofit.responseBodyConverter<E>(clazz, arrayOf())
            return response.copy(error = FetcherError.EntityHttpError<E>(
                exception = error.exception,
                code = error.code,
                message = error.message,
                errorResult = converter.convert((error.exception as HttpException).response()!!.errorBody()!!)
            ))
        } else response
    }
}

inline fun <K: Any, I: Any, reified E: Any> Fetcher<K, I>.deserializeError(retrofit: Retrofit) =
    DeserializeErrorFetcher(this, retrofit, E::class.java)
