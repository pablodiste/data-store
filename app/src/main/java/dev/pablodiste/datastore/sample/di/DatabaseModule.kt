package dev.pablodiste.datastore.sample.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.pablodiste.datastore.sample.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    lateinit var roomDb: AppDatabase

    @Provides
    @Singleton
    fun provideRoom(@ApplicationContext applicationContext: Context): AppDatabase {
        roomDb = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "store-sample")
            .fallbackToDestructiveMigration()
            .build()
        return roomDb
    }

}