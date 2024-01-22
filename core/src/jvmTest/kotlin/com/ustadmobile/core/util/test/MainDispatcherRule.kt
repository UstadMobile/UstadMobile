package com.ustadmobile.core.util.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MainDispatcherRule: TestWatcher() {

    private lateinit var mainThreadSurrogate: CoroutineDispatcher

    override fun starting(description: Description) {
        mainThreadSurrogate = newSingleThreadContext("MainDispatcherRule UI thread")
        Dispatchers.setMain(mainThreadSurrogate)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
        mainThreadSurrogate.cancel()
    }
}