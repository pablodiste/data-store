package com.pablodiste.android.sample

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.rx.RealmObservableFactory

class SampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
            .name("store-sample.realm")
            .deleteRealmIfMigrationNeeded() // Deletes local db if any schema changes
            .build()
        Realm.setDefaultConfiguration(realmConfig)
    }

}