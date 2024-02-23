package dev.pablodiste.datastore.sample.network

import android.util.Log
import dev.pablodiste.datastore.FetcherServiceProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.gson.gson

private const val TIME_OUT = 60_000

class KtorManager: FetcherServiceProvider {

    private val ktorHttpClient = HttpClient(Android) {

        expectSuccess = true

        install(ContentNegotiation) {
            gson()
            engine {
                connectTimeout = TIME_OUT
                socketTimeout = TIME_OUT
            }
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("Logger Ktor =>", message)
                }
            }
            level = LogLevel.ALL
        }

        install(ResponseObserver) {
            onResponse { response ->
                Log.d("HTTP status:", "${response.status.value}")
            }
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    override fun <T> createService(service: Class<T>): T {
        return service.getConstructor(HttpClient::class.java).newInstance(ktorHttpClient)
    }

}
