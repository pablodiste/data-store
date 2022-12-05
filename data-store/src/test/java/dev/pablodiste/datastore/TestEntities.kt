package dev.pablodiste.datastore

import dev.pablodiste.datastore.exceptions.FetcherError

data class TestKey(val id: Int)
data class TestEntity(val id: Int, val name: String)

fun successResult() = FetcherResult.Data(TestEntity(1, "One"))
fun serverError() = FetcherResult.Error(FetcherError.HttpError(Exception("Server Error"), 500, "Server Error"))
fun customError() = FetcherResult.Error(FetcherError.HttpError(Exception("Custom Server Error"), 503, "Custom Server Error"))
