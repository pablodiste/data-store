package dev.pablodiste.datastore

import org.junit.Rule

open class CoroutineTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
}