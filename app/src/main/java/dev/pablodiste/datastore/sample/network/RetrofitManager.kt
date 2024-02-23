package dev.pablodiste.datastore.sample.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.pablodiste.datastore.FetcherServiceProvider
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val SWAPI = "swapi.py4e.com/api/"
private const val JSONPLACEHOLDER = "jsonplaceholder.typicode.com/"
private const val DUMMYJSON = "dummyjson.com/"

class RetrofitManager : FetcherServiceProvider {

    private val retrofitSW: Retrofit
    private val retrofitJP: Retrofit
    private val retrofitDJ: Retrofit
    private val gson: Gson = GsonBuilder().setLenient().create()
    private val okHttpClientBuilder = OkHttpClient.Builder()

    init {
        retrofitSW = okHttpClientBuilder.buildRetrofit(SWAPI)
        retrofitJP = okHttpClientBuilder.buildRetrofit(JSONPLACEHOLDER)
        retrofitDJ = okHttpClientBuilder.buildRetrofit(DUMMYJSON)
    }

    override fun <T> createService(service: Class<T>): T {
        return when (service.simpleName) {
            RoomStarWarsService::class.java.simpleName, StarWarsService::class.java.simpleName -> retrofitSW.create(service)
            DummyJsonService::class.java.simpleName -> retrofitDJ.create(service)
            else -> retrofitJP.create(service)
        }
    }

    private fun OkHttpClient.Builder.buildRetrofit(url: String) = Retrofit.Builder()
        .baseUrl("https://$url")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory(build())
        .build()

}