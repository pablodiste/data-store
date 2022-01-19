package com.pablodiste.android.sample.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.models.room.Post
import com.pablodiste.android.sample.repositories.store.room.PostCache
import com.pablodiste.android.sample.repositories.store.room.PostsCache
import com.pablodiste.android.sample.repositories.store.room.RoomPeopleStore
import com.pablodiste.android.sample.repositories.store.room.RoomPersonStore

@Database(entities = [People::class, Post::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peopleCache(): RoomPeopleStore.PeopleCache
    abstract fun personCache(): RoomPersonStore.PersonCache
    abstract fun postsCache(): PostsCache
    abstract fun postCache(): PostCache
}
