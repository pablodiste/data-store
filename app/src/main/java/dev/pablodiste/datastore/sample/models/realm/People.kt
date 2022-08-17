package dev.pablodiste.datastore.sample.models.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class People: RealmObject() {

    @PrimaryKey
    var id: String = ""
    var name: String? = null
    var height: String? = null
    var mass: String? = null
    var hair_color: String? = null
    var eye_color: String? = null
    var birth_year: String? = null
    var gender: String? = null
    var url: String? = null

    override fun toString(): String {
        return "People(name=$name)"
    }

    fun parseId() {
        id = url?.let { "/([^/]+)/?$".toRegex().find(it)?.groups?.get(1)?.value } ?: ""
    }

}