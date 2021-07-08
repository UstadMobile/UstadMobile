package com.ustadmobile.util.test.rules

import com.ustadmobile.core.util.DiTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.*
import java.util.concurrent.Executors

fun DI.MainBuilder.bindPresenterCoroutineRule(dispatcherRule: CoroutineDispatcherRule) {
    bind<CoroutineScope>(tag = DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with provider {
        CoroutineScope(dispatcherRule.dispatcher)
    }
}

/**
 * Simple test rule to provide a single thread coroutine dispatcher based on an executor
 */
class CoroutineDispatcherRule : TestWatcher() {

    lateinit var dispatcher: ExecutorCoroutineDispatcher
        private set

    override fun starting(description: Description?) {
        dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

    override fun finished(description: Description?) {
        dispatcher.close()
    }
}