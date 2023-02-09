package dev.pablodiste.datastore.crud

import dev.pablodiste.datastore.CrudFetcher2
import dev.pablodiste.datastore.Mapper
import dev.pablodiste.datastore.SameEntityMapper
import dev.pablodiste.datastore.SourceOfTruth
import dev.pablodiste.datastore.Store
import dev.pablodiste.datastore.impl.StoreBuilder

open class CrudStoreBuilder<K: Any, I: Any, T: Any>(
    protected val crudFetcher: CrudFetcher2<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    protected val keyBuilder: (T) -> K
): StoreBuilder<K, I, T>(crudFetcher.readFetcher, sourceOfTruth, mapper) {

    override fun build(): Store<K, T> {
        return CrudStoreImpl(crudFetcher, sourceOfTruth, mapper, keyBuilder)
    }

    companion object {
        fun <K: Any, I: Any, T: Any> from(
            crudFetcher: CrudFetcher2<K, I>,
            sourceOfTruth: SourceOfTruth<K, T>,
            mapper: Mapper<I, T>,
            keyBuilder: (T) -> K
        ): CrudStoreBuilder<K, I, T> =
            CrudStoreBuilder(crudFetcher, sourceOfTruth, mapper, keyBuilder)
    }
}

class SimpleCrudStoreBuilder<K: Any, T: Any>(
    crudFetcher: CrudFetcher2<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>,
    keyBuilder: (T) -> K
): CrudStoreBuilder<K, T, T>(crudFetcher, sourceOfTruth, SameEntityMapper(), keyBuilder) {

    override fun build(): SimpleCrudStoreImpl<K, T> {
        return SimpleCrudStoreImpl(crudFetcher, sourceOfTruth, keyBuilder)
    }

    companion object {
        fun <K: Any, T: Any> from(
            crudFetcher: CrudFetcher2<K, T>,
            sourceOfTruth: SourceOfTruth<K, T>,
            keyBuilder: (T) -> K
        ): SimpleCrudStoreBuilder<K, T> = SimpleCrudStoreBuilder(crudFetcher, sourceOfTruth, keyBuilder)
    }
}
