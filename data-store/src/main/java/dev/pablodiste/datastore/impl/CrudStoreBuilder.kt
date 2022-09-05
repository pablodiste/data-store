package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.*
import kotlinx.coroutines.CoroutineScope

open class CrudStoreBuilder<K: Any, I: Any, T: Any>(
    protected val applicationScope: CoroutineScope,
    protected val crudFetcher: CrudFetcher<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    protected val keyBuilder: (T) -> K
): StoreBuilder<K, I, T>(crudFetcher, sourceOfTruth, mapper) {

    override fun build(): Store<K, T> {
        return CrudStoreImpl(applicationScope, crudFetcher, sourceOfTruth, mapper, keyBuilder)
    }

    companion object {
        fun <K: Any, I: Any, T: Any> from(
            applicationScope: CoroutineScope,
            crudFetcher: CrudFetcher<K, I>,
            sourceOfTruth: SourceOfTruth<K, T>,
            mapper: Mapper<I, T>,
            keyBuilder: (T) -> K
        ): CrudStoreBuilder<K, I, T> = CrudStoreBuilder(applicationScope, crudFetcher, sourceOfTruth, mapper, keyBuilder)
    }
}

class SimpleCrudStoreBuilder<K: Any, T: Any>(
    applicationScope: CoroutineScope,
    fetcher: CrudFetcher<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>,
    keyBuilder: (T) -> K
): CrudStoreBuilder<K, T, T>(applicationScope, fetcher, sourceOfTruth, SameEntityMapper(), keyBuilder) {

    override fun build(): Store<K, T> {
        return SimpleDirectCrudStoreImpl(crudFetcher, sourceOfTruth, keyBuilder)
    }

    companion object {
        fun <K: Any, T: Any> from(
            applicationScope: CoroutineScope,
            fetcher: CrudFetcher<K, T>,
            sourceOfTruth: SourceOfTruth<K, T>,
            keyBuilder: (T) -> K
        ): SimpleCrudStoreBuilder<K, T> = SimpleCrudStoreBuilder(applicationScope, fetcher, sourceOfTruth, keyBuilder)
    }
}
