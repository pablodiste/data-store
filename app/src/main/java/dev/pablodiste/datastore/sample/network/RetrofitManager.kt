package dev.pablodiste.datastore.sample.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.pablodiste.datastore.FetcherServiceProvider
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager : FetcherServiceProvider {

    private val retrofitSW: Retrofit
    private val retrofitJP: Retrofit
    private val retrofitDJ: Retrofit
    private val gson: Gson = GsonBuilder().setLenient().create()

    init {
        val okHttpClientBuilder = OkHttpClient.Builder()
        retrofitSW = okHttpClientBuilder.buildRetrofit("swapi.py4e.com/api/")
        retrofitJP = okHttpClientBuilder.buildRetrofit("jsonplaceholder.typicode.com/")
        retrofitDJ = okHttpClientBuilder.buildRetrofit("dummyjson.com/")
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

    val dummyJSONService by lazy { retrofitDJ.create(DummyJsonService::class.java) }
    val jsonPlaceholderService by lazy { retrofitJP.create(JsonPlaceholderService::class.java) }
    val starWarsService by lazy { retrofitSW.create(StarWarsService::class.java) }
    val roomStarWarsService by lazy { retrofitSW.create(RoomStarWarsService::class.java) }
}