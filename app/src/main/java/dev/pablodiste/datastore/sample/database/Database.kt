package dev.pablodiste.datastore.sample.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.pablodiste.datastore.sample.models.room.DummyPost
import dev.pablodiste.datastore.sample.models.room.People
import dev.pablodiste.datastore.sample.models.room.Post
import dev.pablodiste.datastore.sample.models.room.Starship
import dev.pablodiste.datastore.sample.repositories.store.room.dao.StarshipSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.room.dao.DummyPostSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.room.dao.DummyPostsSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.room.dao.PeopleSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.room.dao.PersonSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.room.dao.PostSourceOfTruth
import dev.pablodiste.datastore.sample.repositories.store.room.dao.PostsSourceOfTruth

@Database(entities = [People::class, Post::class, Starship::class, DummyPost::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peopleSourceOfTruth(): PeopleSourceOfTruth
    abstract fun personSourceOfTruth(): PersonSourceOfTruth
    abstract fun starshipSourceOfTruth(): StarshipSourceOfTruth
    abstract fun postsSourceOfTruth(): PostsSourceOfTruth
    abstract fun postSourceOfTruth(): PostSourceOfTruth
    abstract fun dummyPostsSourceOfTruth(): DummyPostsSourceOfTruth
    abstract fun dummyPostSourceOfTruth(): DummyPostSourceOfTruth
}
