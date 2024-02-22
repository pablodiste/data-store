package dev.pablodiste.datastore.sample.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.pablodiste.datastore.sample.network.DummyJsonService
import dev.pablodiste.datastore.sample.network.JsonPlaceholderService
import dev.pablodiste.datastore.sample.network.RoomStarWarsService
import dev.pablodiste.datastore.sample.network.StarWarsService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitApiModule {

    private val gson: Gson = GsonBuilder().setLenient().create()
    private val okHttpClientBuilder = OkHttpClient.Builder()

    @Provides
    @DummyJson
    @Singleton
    fun provideDummyJsonRetrofit(): Retrofit = okHttpClientBuilder.buildRetrofit("dummyjson.com/")

    @Provides
    @JsonPlaceholder
    @Singleton
    fun provideJsonPlaceholderRetrofit(): Retrofit = okHttpClientBuilder.buildRetrofit("jsonplaceholder.typicode.com/")

    @Provides
    @StarWars
    @Singleton
    fun provideStarWarsRetrofit(): Retrofit = okHttpClientBuilder.buildRetrofit("swapi.py4e.com/api/")

    @Provides
    @Singleton
    fun provideDummyJsonApiService(@DummyJson retrofit: Retrofit): DummyJsonService = retrofit.create(DummyJsonService::class.java)

    @Provides
    @Singleton
    fun provideJsonPlaceholderApiService(@JsonPlaceholder retrofit: Retrofit): JsonPlaceholderService = retrofit.create(JsonPlaceholderService::class.java)

    @Provides
    @Singleton
    fun provideStarWarsApiService(@StarWars retrofit: Retrofit): StarWarsService = retrofit.create(StarWarsService::class.java)

    @Provides
    @Singleton
    fun provideRoomStarWarsApiService(@StarWars retrofit: Retrofit): RoomStarWarsService = retrofit.create(RoomStarWarsService::class.java)

    private fun OkHttpClient.Builder.buildRetrofit(url: String) = Retrofit.Builder()
        .baseUrl("https://$url")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory(build())
        .build()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DummyJson

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class JsonPlaceholder

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StarWars
