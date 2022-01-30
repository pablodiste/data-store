package com.pablodiste.android.sample.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pablodiste.android.adapters.retrofit.RetrofitServiceProvider
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager : RetrofitServiceProvider {

    private val retrofitSW: Retrofit
    private val retrofitJP: Retrofit
    private val gson: Gson = GsonBuilder().setLenient().create()

    init {
        val okHttpClientBuilder = OkHttpClient.Builder()
        retrofitSW = okHttpClientBuilder.buildRetrofit("swapi.py4e.com/api/")
        retrofitJP = okHttpClientBuilder.buildRetrofit("jsonplaceholder.typicode.com/")
    }

    override fun <T> createService(service: Class<T>): T {
        return when (service.simpleName) {
            RoomStarWarsService::class.java.simpleName, StarWarsService::class.java.simpleName -> retrofitSW.create(service)
            else -> retrofitJP.create(service)
        }
    }

    private fun OkHttpClient.Builder.buildRetrofit(url: String) = Retrofit.Builder()
        .baseUrl("https://$url")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory(build())
        .build()

}