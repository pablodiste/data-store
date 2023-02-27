package dev.pablodiste.datastore.fetchers

import dev.pablodiste.datastore.exceptions.FetcherError

class FetcherException(val fetcherError: FetcherError): Exception()
