package com.ustadmobile.core.util.test

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ChangeListenerRequest
import com.ustadmobile.door.ext.handleTablesChanged
import kotlinx.coroutines.*

suspend fun UmAppDatabase.waitUntil(timeout: Long, tableNames: List<String>, checker: () -> Boolean) {
    val completableDeferred = CompletableDeferred<Boolean>()
    val changeListener = ChangeListenerRequest(tableNames) {
        if(checker())
            completableDeferred.complete(true)
    }

    addChangeListener(changeListener)
    this.handleTablesChanged(tableNames)
    withTimeout(timeout) { completableDeferred.await() }

    removeChangeListener(changeListener)
}

suspend fun UmAppDatabase.waitUntilAsync(
    timeout: Long,
    tableNames: List<String>,
    checker: suspend () -> Boolean
) {
    val completableDeferred = CompletableDeferred<Boolean>()
    val changeListener = ChangeListenerRequest(tableNames) {
        GlobalScope.launch {
            if(checker())
                completableDeferred.complete(true)
        }
    }

    addChangeListener(changeListener)
    this.handleTablesChanged(tableNames)
    withTimeout(timeout) { completableDeferred.await() }

    removeChangeListener(changeListener)
}

