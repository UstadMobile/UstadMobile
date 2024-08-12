package com.ustadmobile.core.util.ext

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Coroutine wrapper for Process.waitFor . If the coroutine is canceled, then process.destroy will
 * be invoked to cancel the process itself.
 */
suspend fun Process.waitForAsync() : Int {
    return coroutineScope {
        val completable = CompletableDeferred<Int>()
        launch(Dispatchers.IO) {
            completable.complete(waitFor())
        }

        try {
            completable.await()
        }catch(e: CancellationException) {
            this@waitForAsync.destroy()
            throw e
        }
    }
}
