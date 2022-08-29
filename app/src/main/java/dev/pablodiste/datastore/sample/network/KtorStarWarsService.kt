package dev.pablodiste.datastore.sample.network

import dev.pablodiste.datastore.sample.models.room.StarshipDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorStarWarsService(private val client: HttpClient) {

    suspend fun getStarships(): ApiListResponse<StarshipDTO> {
        return client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = SWAPI_BASE_URL
                port = 443
                path("api/starships/")
            }
        }.body()
    }

    companion object {
        const val SWAPI_BASE_URL = "swapi.py4e.com"
    }
}