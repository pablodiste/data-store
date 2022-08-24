package dev.pablodiste.datastore.sample.models.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.pablodiste.datastore.Mapper

/**
 * Network layer DTO used to parse API data
 */
data class StarshipDTO(
    var name: String? = null,
    var model: String? = null,
    var manufacturer: String? = null,
    var crew: String? = null,
    var passengers: String? = null,
    var url: String? = null,
) {
    val id: Int get() = (url?.let { "/([^/]+)/?$".toRegex().find(it)?.groups?.get(1)?.value } ?: "").toInt()
}

/**
 * DB layer entity used to store data in the DB
 */
@Entity(tableName = "starships")
data class Starship(
    @PrimaryKey var id: Int = 0,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "model") var model: String? = null,
    @ColumnInfo(name = "manufacturer") var manufacturer: String? = null,
    @ColumnInfo(name = "crew") var crew: String? = null,
    @ColumnInfo(name = "passengers") var passengers: String? = null,
) {
    override fun toString(): String {
        return "Starship(id=$id, name=$name, model=$model)"
    }
}

class StarshipMapper: Mapper<List<StarshipDTO>, List<Starship>> {
    override fun toSourceOfTruthEntity(input: List<StarshipDTO>): List<Starship> {
        return input.map { Starship(it.id, it.name, it.model, it.manufacturer, it.crew, it.passengers) }
    }

    override fun toFetcherEntity(sourceOfTruthEntity: List<Starship>): List<StarshipDTO> {
        return sourceOfTruthEntity.map { StarshipDTO(it.name, it.model, it.manufacturer, it.crew, it.passengers) }
    }
}