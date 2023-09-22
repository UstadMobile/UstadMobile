package com.ustadmobile.core.util.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Setup dispatchers main delegation as per:
 *
 * https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/
 *
 * Note: Main dispatcher is not reset - this results in flakey errors compl
 */
abstract class AbstractMainDispatcherTest {

    private lateinit var mainThreadSurrogate: CoroutineDispatcher

    @OptIn(ExperimentalCoroutinesApi::class)
    @DelicateCoroutinesApi
    @BeforeTest
    fun setupMainDispatcher(){
        mainThreadSurrogate = newSingleThreadContext("UI Thread")
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @AfterTest
    fun tearDownMainDispatcher() {
        mainThreadSurrogate.cancel()
    }
}