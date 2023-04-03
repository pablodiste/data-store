package dev.pablodiste.datastore

data class StoreRequest<K>(
    /**
     * This key identifies a request to the fetcher. Same fetcher calls should have the same key.
     * The key should implement toString, hashCode and equals methods.
     */
    val key: K,

    /**
     * When true, the store requests a fetch to update the source of truth.
     */
    val refresh: Boolean = true,

    /**
     * When true, if no data is found on the source of truth, a fetch is requested.
     */
    val fetchWhenNoDataFound: Boolean = true,

    /**
     * When true, the fetcher ignores any rule that limit calls and perform the fetch anyways.
     */
    val forceFetch: Boolean = false,

    /**
     * When true, the store will emit NoData states, caused by rate limiters and other scenarios
     */
    val emitNoDataStates: Boolean = false,

    /**
     * When true, the store will emit Loading when executing a fetcher call
     */
    val emitLoadingStates: Boolean = false
)