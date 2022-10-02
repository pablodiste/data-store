package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.*

open class CrudStoreBuilder<K: Any, I: Any, T: Any>(
    protected val crudFetcher: CrudFetcher<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    protected val keyBuilder: (T) -> K
): StoreBuilder<K, I, T>(crudFetcher, sourceOfTruth, mapper) {

    override fun build(): Store<K, T> {
        return CrudStoreImpl(crudFetcher, sourceOfTruth, mapper, keyBuilder)
    }

    companion object {
        fun <K: Any, I: Any, T: Any> from(
            crudFetcher: CrudFetcher<K, I>,
            sourceOfTruth: SourceOfTruth<K, T>,
            mapper: Mapper<I, T>,
            keyBuilder: (T) -> K
        ): CrudStoreBuilder<K, I, T> =
            CrudStoreBuilder(crudFetcher, sourceOfTruth, mapper, keyBuilder)
    }
}

class SimpleCrudStoreBuilder<K: Any, T: Any>(
    fetcher: CrudFetcher<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>,
    keyBuilder: (T) -> K
): CrudStoreBuilder<K, T, T>(fetcher, sourceOfTruth, SameEntityMapper(), keyBuilder) {

    override fun build(): Store<K, T> {
        return SimpleCrudStoreImpl(crudFetcher, sourceOfTruth, keyBuilder)
    }

    companion object {
        fun <K: Any, T: Any> from(
            fetcher: CrudFetcher<K, T>,
            sourceOfTruth: SourceOfTruth<K, T>,
            keyBuilder: (T) -> K
        ): SimpleCrudStoreBuilder<K, T> = SimpleCrudStoreBuilder(fetcher, sourceOfTruth, keyBuilder)
    }
}
