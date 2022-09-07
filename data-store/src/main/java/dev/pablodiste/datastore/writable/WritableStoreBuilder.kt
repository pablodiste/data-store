package dev.pablodiste.datastore.writable

import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.impl.StoreBuilder
import kotlinx.coroutines.CoroutineScope

open class WritableStoreBuilder<K: Any, I: Any, T: Any>(
    protected val applicationScope: CoroutineScope,
    fetcher: Fetcher<K, I>,
    protected val sender: Sender<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    protected val keyBuilder: (T) -> K
): StoreBuilder<K, I, T>(fetcher, sourceOfTruth, mapper) {

    override fun build(): Store<K, T> {
        return WritableStoreImpl(applicationScope, fetcher, sender, sourceOfTruth, mapper, keyBuilder)
    }

    companion object {
        fun <K: Any, I: Any, T: Any> from(
            applicationScope: CoroutineScope,
            fetcher: Fetcher<K, I>,
            sender: Sender<K, I>,
            sourceOfTruth: SourceOfTruth<K, T>,
            mapper: Mapper<I, T>,
            keyBuilder: (T) -> K
        ): WritableStoreBuilder<K, I, T> = WritableStoreBuilder(applicationScope, fetcher, sender, sourceOfTruth, mapper, keyBuilder)
    }
}

class SimpleWritableStoreBuilder<K: Any, T: Any>(
    applicationScope: CoroutineScope,
    fetcher: Fetcher<K, T>,
    sender: Sender<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>,
    keyBuilder: (T) -> K
): WritableStoreBuilder<K, T, T>(applicationScope, fetcher, sender, sourceOfTruth, SameEntityMapper(), keyBuilder) {

    override fun build(): Store<K, T> {
        return SimpleWritableStoreImpl(applicationScope, fetcher, sender, sourceOfTruth, keyBuilder)
    }

    companion object {
        fun <K: Any, T: Any> from(
            applicationScope: CoroutineScope,
            fetcher: Fetcher<K, T>,
            sender: Sender<K, T>,
            sourceOfTruth: SourceOfTruth<K, T>,
            keyBuilder: (T) -> K
        ): SimpleWritableStoreBuilder<K, T> = SimpleWritableStoreBuilder(applicationScope, fetcher, sender, sourceOfTruth, keyBuilder)
    }
}
