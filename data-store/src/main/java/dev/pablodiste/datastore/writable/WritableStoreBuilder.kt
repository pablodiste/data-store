package dev.pablodiste.datastore.writable

import dev.pablodiste.datastore.*
import dev.pablodiste.datastore.impl.StoreBuilder
import kotlinx.coroutines.CoroutineScope

open class WritableStoreBuilder<K: Any, I: Any, T: Any>(
    protected val clazz: Class<T>,
    protected val applicationScope: CoroutineScope,
    fetcher: Fetcher<K, I>,
    protected val sender: Sender<K, I>,
    sourceOfTruth: SourceOfTruth<K, T>,
    mapper: Mapper<I, T>,
    protected val keyBuilder: (T) -> K
): StoreBuilder<K, I, T>(fetcher, sourceOfTruth, mapper) {

    override fun build(): WritableStore<K, T> {
        return WritableStoreImpl(clazz, applicationScope, fetcher, sender, sourceOfTruth, mapper, keyBuilder)
    }

    companion object {
        inline fun <K: Any, I: Any, reified T: Any> from(
            applicationScope: CoroutineScope,
            fetcher: Fetcher<K, I>,
            sender: Sender<K, I>,
            sourceOfTruth: SourceOfTruth<K, T>,
            mapper: Mapper<I, T>,
            noinline keyBuilder: (T) -> K
        ): WritableStoreBuilder<K, I, T> = WritableStoreBuilder(T::class.java, applicationScope, fetcher, sender, sourceOfTruth, mapper, keyBuilder)
    }
}

class SimpleWritableStoreBuilder<K: Any, T: Any>(
    clazz: Class<T>,
    applicationScope: CoroutineScope,
    fetcher: Fetcher<K, T>,
    sender: Sender<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>,
    keyBuilder: (T) -> K
): WritableStoreBuilder<K, T, T>(clazz, applicationScope, fetcher, sender, sourceOfTruth, SameEntityMapper(), keyBuilder) {

    override fun build(): SimpleWritableStoreImpl<K, T> {
        return SimpleWritableStoreImpl(clazz, applicationScope, fetcher, sender, sourceOfTruth, keyBuilder)
    }

    companion object {
        inline fun <K: Any, reified T: Any> from(
            applicationScope: CoroutineScope,
            fetcher: Fetcher<K, T>,
            sender: Sender<K, T>,
            sourceOfTruth: SourceOfTruth<K, T>,
            noinline keyBuilder: (T) -> K
        ): SimpleWritableStoreBuilder<K, T> = SimpleWritableStoreBuilder(T::class.java, applicationScope, fetcher, sender, sourceOfTruth, keyBuilder)
    }
}
