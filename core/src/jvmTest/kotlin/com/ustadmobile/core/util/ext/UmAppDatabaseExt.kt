package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

suspend fun UmAppDatabase.waitUntil(timeout: Long, tableNames: List<String>, checker: () -> Boolean) {
    val completableDeferred = CompletableDeferred<Boolean>()
    val changeListener = DoorDatabase.ChangeListenerRequest(tableNames) {
        if(checker())
            completableDeferred.complete(true)
    }

    addChangeListener(changeListener)
    changeListener.onChange(tableNames)
    withTimeoutOrNull(timeout) { completableDeferred.await() }

    removeChangeListener(changeListener)
}