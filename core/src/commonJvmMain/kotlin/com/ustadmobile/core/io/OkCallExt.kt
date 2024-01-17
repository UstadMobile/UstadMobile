package com.ustadmobile.core.io

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * Simple extension function to use the default OKHttp dispatcher to run an http call asynchronously.
 */
suspend fun Call.await(): Response {
    val completable = CompletableDeferred<Response>()
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            completable.completeExceptionally(e)
        }

        override fun onResponse(call: Call, response: Response) {
            completable.complete(response)
        }
    })

    try {
        return completable.await()
    }catch(e: Exception) {
        if(e is CancellationException)
            cancel()

        throw e
    }
}