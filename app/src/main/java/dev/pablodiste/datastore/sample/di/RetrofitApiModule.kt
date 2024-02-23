package dev.pablodiste.datastore.sample.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.pablodiste.datastore.sample.network.DummyJsonService
import dev.pablodiste.datastore.sample.network.JsonPlaceholderService
import dev.pablodiste.datastore.sample.network.RetrofitManager
import dev.pablodiste.datastore.sample.network.RoomStarWarsService
import dev.pablodiste.datastore.sample.network.StarWarsService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitApiModule {

    @Provides
    @Singleton
    fun provideRetrofitManager(): RetrofitManager = RetrofitManager()

    @Provides
    @Singleton
    fun provideDummyJsonApiService(retrofitManager: RetrofitManager): DummyJsonService = retrofitManager.createService(DummyJsonService::class.java)

    @Provides
    @Singleton
    fun provideJsonPlaceholderApiService(retrofitManager: RetrofitManager): JsonPlaceholderService = retrofitManager.createService(JsonPlaceholderService::class.java)

    @Provides
    @Singleton
    fun provideStarWarsApiService(retrofitManager: RetrofitManager): StarWarsService = retrofitManager.createService(StarWarsService::class.java)

    @Provides
    @Singleton
    fun provideRoomStarWarsApiService(retrofitManager: RetrofitManager): RoomStarWarsService = retrofitManager.createService(RoomStarWarsService::class.java)

}