package com.pablodiste.android.sample.models.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class People(
    @PrimaryKey var id: String = "",
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "height") var height: String? = null,
    @ColumnInfo(name = "mass") var mass: String? = null,
    @ColumnInfo(name = "hair_color") var hair_color: String? = null,
    @ColumnInfo(name = "eye_color") var eye_color: String? = null,
    @ColumnInfo(name = "birth_year") var birth_year: String? = null,
    @ColumnInfo(name = "gender") var gender: String? = null,
    @ColumnInfo(name = "url") var url: String? = null,
) {
    override fun toString(): String {
        return "People(name=$name)"
    }

    fun parseId() {
        id = url?.let { "/([^/]+)/?$".toRegex().find(it)?.groups?.get(1)?.value } ?: ""
    }
}
