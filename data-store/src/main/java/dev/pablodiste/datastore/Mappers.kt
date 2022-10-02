package dev.pablodiste.datastore

interface Mapper<I: Any, T: Any> {
    fun toSourceOfTruthEntity(input: I): T
    fun toFetcherEntity(sourceOfTruthEntity: T): I
}

class SameEntityMapper<T: Any>: Mapper<T, T> {
    override fun toSourceOfTruthEntity(input: T): T = input
    override fun toFetcherEntity(sourceOfTruthEntity: T): T = sourceOfTruthEntity
}