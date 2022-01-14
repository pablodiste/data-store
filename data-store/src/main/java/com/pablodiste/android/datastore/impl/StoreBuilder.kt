package com.pablodiste.android.datastore.impl

import com.pablodiste.android.datastore.*

open class StoreBuilder<K: Any, I: Any, T: Any>(
    private var fetcher: Fetcher<K, I>,
    private var cache: Cache<K, T>,
    protected open var mapper: Mapper<I, T>
) {

    fun fetcher(fetcher: Fetcher<K, I>): StoreBuilder<K, I, T> {
        this.fetcher = fetcher
        return this
    }

    fun cache(cache: Cache<K, T>): StoreBuilder<K, I, T> {
        this.cache = cache
        return this
    }

    fun mapper(mapper: Mapper<I, T>): StoreBuilder<K, I, T> {
        this.mapper = mapper
        return this
    }

    fun build(): Store<K, T> {
        return StoreImpl(fetcher, cache, mapper)
    }

    companion object {
        fun <K: Any, I: Any, T: Any> from(fetcher: Fetcher<K, I>,
                                          cache: Cache<K, T>,
                                          mapper: Mapper<I, T>): StoreBuilder<K, I, T> =
            StoreBuilder(fetcher, cache, mapper)
    }
}

class SimpleStoreBuilder<K: Any, T: Any>(
    private var fetcher: Fetcher<K, T>,
    private var cache: Cache<K, T>
): StoreBuilder<K, T, T>(fetcher, cache, SameEntityMapper()) {
    companion object {
        fun <K : Any, T : Any> from(fetcher: Fetcher<K, T>, cache: Cache<K, T>): SimpleStoreBuilder<K, T> =
            SimpleStoreBuilder(fetcher, cache)
    }
}
