package dev.pablodiste.datastore

import dev.pablodiste.datastore.coroutines.DefaultCoroutineConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule constructor(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
        val job = SupervisorJob()
        StoreConfig.coroutineConfig = DefaultCoroutineConfig(
            testDispatcher, testDispatcher, testDispatcher, job, CoroutineScope(testDispatcher + job)
        )
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}