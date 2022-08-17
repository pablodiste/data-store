package dev.pablodiste.datastore.sample.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.models.room.Post
import dev.pablodiste.datastore.sample.repositories.store.room.PostCache
import dev.pablodiste.datastore.sample.repositories.store.room.PostsCache
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPeopleStore
import dev.pablodiste.datastore.sample.repositories.store.room.RoomPersonStore

@Database(entities = [People::class, Post::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peopleCache(): RoomPeopleStore.PeopleCache
    abstract fun personCache(): RoomPersonStore.PersonCache
    abstract fun postsCache(): PostsCache
    abstract fun postCache(): PostCache
}
