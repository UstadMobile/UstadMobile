package com.ustadmobile.core.util.test

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ChangeListenerRequest
import com.ustadmobile.door.room.InvalidationTrackerObserver
import kotlinx.coroutines.*

suspend fun UmAppDatabase.waitUntil(timeout: Long, tableNames: List<String>, checker: () -> Boolean) {
    val completableDeferred = CompletableDeferred<Boolean>()

    val invalidationObserver = object: InvalidationTrackerObserver(tableNames.toTypedArray()) {
        override fun onInvalidated(tables: Set<String>) {
            completableDeferred.complete(true)
        }
    }
    getInvalidationTracker().addObserver(invalidationObserver)
    if(checker())
        completableDeferred.complete(true)

    try {
        withTimeout(timeout) { completableDeferred.await() }
    }finally {
        getInvalidationTracker().removeObserver(invalidationObserver)
    }

}

private suspend fun UmAppDatabase.waitUntilWithWaitFn(
    timeout: Long,
    tableNames: List<String>,
    checker: suspend () -> Boolean,
    waitFn: suspend (time: Long, suspend() -> Unit) -> Unit
) {
    val completableDeferred = CompletableDeferred<Boolean>()
    val invalidationObserver = object: InvalidationTrackerObserver(tableNames.toTypedArray()) {
        override fun onInvalidated(tables: Set<String>) {
            completableDeferred.complete(true)
        }
    }
    getInvalidationTracker().addObserver(invalidationObserver)

    if(checker())
        completableDeferred.complete(true)

    try {
        waitFn(timeout) {
            completableDeferred.await()
        }
    }finally {
        getInvalidationTracker().removeObserver(invalidationObserver)
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

