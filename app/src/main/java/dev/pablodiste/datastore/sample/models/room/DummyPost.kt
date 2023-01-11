package dev.pablodiste.datastore.sample.models.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dummy_posts")
data class DummyPost(
    @ColumnInfo(name = "id") @PrimaryKey var id: Int = 0,
    @ColumnInfo(name = "title") var title: String? = null,
    @ColumnInfo(name = "body") var body: String? = null,
    @ColumnInfo(name = "user_id") var userId: Int? = null,
    @ColumnInfo(name = "reactions") var reactions: Int = 0,
) {
    override fun toString(): String {
        return "Post(id=$id, title=$title)"
    }
}