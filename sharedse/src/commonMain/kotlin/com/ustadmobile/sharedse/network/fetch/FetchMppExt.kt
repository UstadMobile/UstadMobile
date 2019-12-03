package com.ustadmobile.sharedse.network.fetch

import com.tonyodev.fetch2.Error
import kotlinx.coroutines.CompletableDeferred
import kotlinx.io.IOException


suspend fun FetchMpp.pause(requestId: Int): DownloadMpp {
    val completableDeferred = CompletableDeferred<DownloadMpp>()
    this.pause(requestId, object :FuncMpp<DownloadMpp> {
        override fun call(result: DownloadMpp) {
            completableDeferred.complete(result)
        }
    },
    object: FuncMpp<Error> {
        override fun call(result: Error) {
            completableDeferred.completeExceptionally(IOException("Fetch exception: $result"))
        }
    })

    return completableDeferred.await()
}

suspend fun FetchMpp.enqueue(request: RequestMpp): RequestMpp {
    val completable = CompletableDeferred<RequestMpp>()
    this.enqueue(request, object: FuncMpp<RequestMpp> {
        override fun call(result: RequestMpp) {
            completable.complete(result)
        }
    },
    object: FuncMpp<Error> {
        override fun call(result: Error) {
            completable.completeExceptionally(IOException("Could not enqueue: $result"))
        }
    })

    return completable.await()
}