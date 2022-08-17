package dev.pablodiste.datastore.sample.models.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Planet: RealmObject() {

    @PrimaryKey
    var name: String? = null
    var rotation_period: String? = null
    var orbital_period: String? = null
    var diameter: String? = null
    var climate: String? = null
    var gravity: String? = null
    var terrain: String? = null
    var surface_water: String? = null
    var population: String? = null

    override fun toString(): String {
        return "People(name=$name)"
    }

}