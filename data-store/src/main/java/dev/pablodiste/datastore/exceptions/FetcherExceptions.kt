package dev.pablodiste.datastore.exceptions

sealed class FetcherError(val exception: Exception) {
    class HttpError(exception: Exception, val code: Int, val message: String) : FetcherError(exception)
    class EntityHttpError<E: Any>(exception: Exception, val code: Int, val message: String, val errorResult: E?) : FetcherError(exception)
    class IOError(exception: Exception) : FetcherError(exception)
    class ClientError(exception: Exception): FetcherError(exception)
    class UnknownError(exception: Exception): FetcherError(exception)
}
