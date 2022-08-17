package dev.pablodiste.datastore.sample.models.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey var id: Int = 0,
    @ColumnInfo(name = "user_id") var userId: Int? = null,
    @ColumnInfo(name = "title") var title: String? = null,
    @ColumnInfo(name = "body") var body: String? = null,
) {
    override fun toString(): String {
        return "Post(id=$id, title=$title)"
    }
}