package com.ustadmobile.core.util.test

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ChangeListenerRequest
import com.ustadmobile.door.ext.handleTablesChanged
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

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
