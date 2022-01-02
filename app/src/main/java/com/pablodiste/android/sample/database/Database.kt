package com.pablodiste.android.sample.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.RoomDatabase
import com.pablodiste.android.sample.models.room.People
import com.pablodiste.android.sample.repositories.store.RoomPeopleStore
import kotlinx.coroutines.flow.Flow

@Database(entities = [People::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peopleDao(): PeopleDao
    abstract fun peopleCache(): RoomPeopleStore.PeopleDao
}

@Dao
interface PeopleDao {
    @Query("SELECT * FROM people")
    fun getAll(): Flow<List<People>>
}