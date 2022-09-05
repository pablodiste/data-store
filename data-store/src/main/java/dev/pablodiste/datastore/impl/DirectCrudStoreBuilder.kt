package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.*

open class DirectCrudStoreBuilder<K: Any, I: Any, T: Any>(
    protected val crudFetcher: CrudFetcher<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    protected val keyBuilder: (T) -> K
): StoreBuilder<K, I, T>(crudFetcher, sourceOfTruth, mapper) {

    override fun build(): Store<K, T> {
        return DirectCrudStoreImpl(crudFetcher, sourceOfTruth, mapper, keyBuilder)
    }

    companion object {
        fun <K: Any, I: Any, T: Any> from(
            crudFetcher: CrudFetcher<K, I>,
            sourceOfTruth: SourceOfTruth<K, T>,
            mapper: Mapper<I, T>,
            keyBuilder: (T) -> K
        ): DirectCrudStoreBuilder<K, I, T> =
            DirectCrudStoreBuilder(crudFetcher, sourceOfTruth, mapper, keyBuilder)
    }
}

class SimpleDirectCrudStoreBuilder<K: Any, T: Any>(
    fetcher: CrudFetcher<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>,
    keyBuilder: (T) -> K
): DirectCrudStoreBuilder<K, T, T>(fetcher, sourceOfTruth, SameEntityMapper(), keyBuilder) {

    override fun build(): Store<K, T> {
        return SimpleDirectCrudStoreImpl(crudFetcher, sourceOfTruth, keyBuilder)
    }

    companion object {
        fun <K: Any, T: Any> from(
            fetcher: CrudFetcher<K, T>,
            sourceOfTruth: SourceOfTruth<K, T>,
            keyBuilder: (T) -> K
        ): SimpleDirectCrudStoreBuilder<K, T> = SimpleDirectCrudStoreBuilder(fetcher, sourceOfTruth, keyBuilder)
    }
}
