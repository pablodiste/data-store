package com.pablodiste.android.sample.models.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class People: RealmObject() {

    @PrimaryKey
    var name: String? = null
    var height: String? = null
    var mass: String? = null
    var hair_color: String? = null
    var eye_color: String? = null
    var birth_year: String? = null
    var gender: String? = null

    override fun toString(): String {
        return "People(name=$name)"
    }

}