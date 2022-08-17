package dev.pablodiste.datastore.impl

import dev.pablodiste.datastore.*

open class StoreBuilder<K: Any, I: Any, T: Any>(
    protected var fetcher: Fetcher<K, I>,
    protected var cache: Cache<K, T>,
    protected var mapper: Mapper<I, T>
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

    open fun build(): Store<K, T> {
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
    fetcher: Fetcher<K, T>,
    cache: Cache<K, T>
): StoreBuilder<K, T, T>(fetcher, cache, SameEntityMapper()) {

    override fun build(): Store<K, T> {
        return SimpleStoreImpl(fetcher, cache)
    }

    companion object {
        fun <K: Any, T: Any> from(fetcher: Fetcher<K, T>, cache: Cache<K, T>): SimpleStoreBuilder<K, T> =
            SimpleStoreBuilder(fetcher, cache)
    }
}

open class CrudStoreBuilder<K: Any, I: Any, T: Any>(
    protected val crudFetcher: CrudFetcher<K, I>,
    cache: Cache<K, T>,
    mapper: Mapper<I, T>,
    protected val keyBuilder: (T) -> K
): StoreBuilder<K, I, T>(crudFetcher, cache, mapper) {

    override fun build(): Store<K, T> {
        return CrudStoreImpl(crudFetcher, cache, mapper, keyBuilder)
    }

    companion object {
        fun <K: Any, I: Any, T: Any> from(
            crudFetcher: CrudFetcher<K, I>,
            cache: Cache<K, T>,
            mapper: Mapper<I, T>,
            keyBuilder: (T) -> K
        ): CrudStoreBuilder<K, I, T> =
            CrudStoreBuilder(crudFetcher, cache, mapper, keyBuilder)
    }
}

class SimpleCrudStoreBuilder<K: Any, T: Any>(
    fetcher: CrudFetcher<K, T>,
    cache: Cache<K, T>,
    keyBuilder: (T) -> K
): CrudStoreBuilder<K, T, T>(fetcher, cache, SameEntityMapper(), keyBuilder) {

    override fun build(): Store<K, T> {
        return SimpleCrudStoreImpl(crudFetcher, cache, keyBuilder)
    }

    companion object {
        fun <K: Any, T: Any> from(
            fetcher: CrudFetcher<K, T>,
            cache: Cache<K, T>,
            keyBuilder: (T) -> K
        ): SimpleCrudStoreBuilder<K, T> = SimpleCrudStoreBuilder(fetcher, cache, keyBuilder)
    }
}