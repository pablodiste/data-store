package dev.pablodiste.datastore.sample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.pablodiste.datastore.sample.database.AppDatabase
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

@HiltAndroidApp
class SampleApplication: Application() {

    @Inject lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        initRealm()
        roomDb = database
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