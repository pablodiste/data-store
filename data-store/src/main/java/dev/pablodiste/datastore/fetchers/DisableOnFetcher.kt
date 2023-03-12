package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.Fetcher
import dev.pablodiste.datastore.FetcherResult

fun <K: Any, I: Any> Fetcher<K, I>.disableOn(predicate: () -> Boolean): Fetcher<K, I> {
    return Fetcher {
        when (predicate.invoke()) {
            true -> FetcherResult.NoData("Fetcher disabled by disabledOn operator")
            else -> this.fetch(it)
        }
    }
}
