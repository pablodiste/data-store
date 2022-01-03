package com.pablodiste.android.sample

import android.app.Application
import androidx.room.Room
import com.pablodiste.android.sample.database.AppDatabase
import io.realm.Realm
import io.realm.RealmConfiguration

class SampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initRealm()
        initRoom()
    }

    private fun initRoom() {
        roomDb = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "store-sample")
            .fallbackToDestructiveMigration()
            .build()
    }

    private fun initRealm() {
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
            .name("store-sample.realm")
            .deleteRealmIfMigrationNeeded() // Deletes local db if any schema changes
            .build()
        Realm.setDefaultConfiguration(realmConfig)
    }

    companion object {
        lateinit var roomDb: AppDatabase
    }
}