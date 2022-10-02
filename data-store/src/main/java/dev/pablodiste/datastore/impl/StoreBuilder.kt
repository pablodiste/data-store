package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.*

open class StoreBuilder<K: Any, I: Any, T: Any>(
    protected var fetcher: Fetcher<K, I>,
    protected var sourceOfTruth: SourceOfTruth<K, T>,
    protected var mapper: Mapper<I, T>
) {

    fun fetcher(fetcher: Fetcher<K, I>): StoreBuilder<K, I, T> {
        this.fetcher = fetcher
        return this
    }

    fun sourceOfTruth(sourceOfTruth: SourceOfTruth<K, T>): StoreBuilder<K, I, T> {
        this.sourceOfTruth = sourceOfTruth
        return this
    }

    fun mapper(mapper: Mapper<I, T>): StoreBuilder<K, I, T> {
        this.mapper = mapper
        return this
    }

    open fun build(): Store<K, T> {
        return StoreImpl(fetcher, sourceOfTruth, mapper)
    }

    companion object {
        fun <K: Any, I: Any, T: Any> from(fetcher: Fetcher<K, I>,
                                          sourceOfTruth: SourceOfTruth<K, T>,
                                          mapper: Mapper<I, T>): StoreBuilder<K, I, T> =
            StoreBuilder(fetcher, sourceOfTruth, mapper)
    }
}

class SimpleStoreBuilder<K: Any, T: Any>(
    fetcher: Fetcher<K, T>,
    sourceOfTruth: SourceOfTruth<K, T>
): StoreBuilder<K, T, T>(fetcher, sourceOfTruth, SameEntityMapper()) {

    override fun build(): SimpleStoreImpl<K, T> {
        return SimpleStoreImpl(fetcher, sourceOfTruth)
    }

    companion object {
        fun <K: Any, T: Any> from(fetcher: Fetcher<K, T>, sourceOfTruth: SourceOfTruth<K, T>): SimpleStoreBuilder<K, T> =
            SimpleStoreBuilder(fetcher, sourceOfTruth)
    }
}
