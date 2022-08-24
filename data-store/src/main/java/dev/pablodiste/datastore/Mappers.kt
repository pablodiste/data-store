package dev.pablodiste.datastore

interface Mapper<I: Any, T: Any> {
    fun toSourceOfTruthEntity(input: I): T
    fun toFetcherEntity(cacheEntity: T): I
}

class SameEntityMapper<T: Any>: Mapper<T, T> {
    override fun toSourceOfTruthEntity(input: T): T = input
    override fun toFetcherEntity(cacheEntity: T): T = cacheEntity
}