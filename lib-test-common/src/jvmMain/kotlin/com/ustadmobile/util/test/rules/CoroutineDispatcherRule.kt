package com.ustadmobile.util.test.rules

import com.ustadmobile.core.util.DiTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.kodein.di.*
import java.util.concurrent.Executors
import kotlin.jvm.Volatile

fun DI.MainBuilder.bindPresenterCoroutineRule(dispatcherRule: CoroutineDispatcherRule) {
    bind<CoroutineScope>(tag = DiTag.TAG_PRESENTER_COROUTINE_SCOPE) with singleton {
        //CoroutineScope(dispatcherRule.dispatcher)

        //TODO: This is really bad and completely defeats the point of presenter scope
        GlobalScope
    }
}

/**
 * Simple test rule to provide a single thread coroutine dispatcher based on an executor
 */
class CoroutineDispatcherRule : TestWatcher() {

    internal val dispatcher: ExecutorCoroutineDispatcher
        get() {
            return _dispatcherInternal ?: Executors.newSingleThreadExecutor().asCoroutineDispatcher().also {
                _dispatcherInternal = it
            }
        }

    @Volatile
    private var _dispatcherInternal: ExecutorCoroutineDispatcher? = null

    override fun starting(description: Description?) {

    }

    override fun finished(description: Description?) {
        _dispatcherInternal?.close()
        _dispatcherInternal = null
    }
}