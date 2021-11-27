package com.pablodiste.android.datastore

interface Mapper<I: Any, T: Any> {
    fun toCacheEntity(input: I): T
    fun toFetcherEntity(cacheEntity: T): I
}

class SameEntityMapper<T: Any>: Mapper<T, T> {
    override fun toCacheEntity(input: T): T = input
    override fun toFetcherEntity(cacheEntity: T): T = cacheEntity
}