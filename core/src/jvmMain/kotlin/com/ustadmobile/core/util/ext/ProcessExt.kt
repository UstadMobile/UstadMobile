package com.ustadmobile.core.util.ext

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun Process.waitForAsync() : Int {
    return coroutineScope {
        val completable = CompletableDeferred<Int>()
        launch {
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
