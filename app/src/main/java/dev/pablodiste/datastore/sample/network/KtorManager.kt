package dev.pablodiste.datastore.sample.network

import android.util.Log
import dev.pablodiste.datastore.FetcherServiceProvider
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.observer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*

private const val TIME_OUT = 60_000

object KtorManager: FetcherServiceProvider {

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
