package com.ustadmobile.core.util.test

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.setMain
import kotlin.concurrent.Volatile
import kotlin.test.BeforeTest

/**
 * Setup dispatchers main delegation as per:
 *
 * https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/
 *
 * Note: Main dispatcher is not reset - this results in flakey errors compl
 */
abstract class AbstractMainDispatcherTest {

    @OptIn(DelicateCoroutinesApi::class)
    @BeforeTest
    fun setupMainDispatcher(){
        setupMainDispatcherIfNeeded()
    }

    companion object {

        @Volatile
        private var mainDispatcherSet = false

        @OptIn(ExperimentalCoroutinesApi::class)
        @DelicateCoroutinesApi
        fun setupMainDispatcherIfNeeded() {
            if(mainDispatcherSet)
                return

            val mainDispatcher = newSingleThreadContext("UI Thread")
            Dispatchers.setMain(mainDispatcher)
            mainDispatcherSet = true
        }

    }

}