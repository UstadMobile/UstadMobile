package com.ustadmobile.core.util.test

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ChangeListenerRequest
import kotlinx.coroutines.*

suspend fun UmAppDatabase.waitUntil(timeout: Long, tableNames: List<String>, checker: () -> Boolean) {
    val completableDeferred = CompletableDeferred<Boolean>()
    val changeListener = ChangeListenerRequest(tableNames) {
        if(checker())
            completableDeferred.complete(true)
    }

    addChangeListener(changeListener)
    if(checker())
        completableDeferred.complete(true)

    withTimeout(timeout) { completableDeferred.await() }

    removeChangeListener(changeListener)
}

private suspend fun UmAppDatabase.waitUntilWithWaitFn(
    timeout: Long,
    tableNames: List<String>,
    checker: suspend () -> Boolean,
    waitFn: suspend (time: Long, suspend() -> Unit) -> Unit
) {
    val completableDeferred = CompletableDeferred<Boolean>()
    val changeListener = ChangeListenerRequest(tableNames) {
        GlobalScope.launch {
            if(checker())
                completableDeferred.complete(true)
        }
    }

    addChangeListener(changeListener)
    if(checker())
        completableDeferred.complete(true)

    try {
        waitFn(timeout) {
            completableDeferred.await()
        }
    }finally {
        removeChangeListener(changeListener)
    }
}
suspend fun UmAppDatabase.waitUntilAsyncOrTimeout(
    timeout: Long,
    tableNames: List<String>,
    checker: suspend () -> Boolean
) {
    waitUntilWithWaitFn(timeout, tableNames, checker) { time, block ->
        withTimeout(time) { block() }
    }
}

suspend fun UmAppDatabase.waitUntilAsyncOrContinueAfter(
    timeout: Long,
    tableNames: List<String>,
    checker: suspend () -> Boolean
) {
    waitUntilWithWaitFn(timeout, tableNames, checker) { time, block ->
        withTimeoutOrNull(time) { block() }
    }
}

