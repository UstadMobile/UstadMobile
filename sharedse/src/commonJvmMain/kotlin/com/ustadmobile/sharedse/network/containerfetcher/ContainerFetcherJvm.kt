package com.ustadmobile.sharedse.network.containerfetcher

import kotlinx.coroutines.*
import java.util.concurrent.*
import com.ustadmobile.sharedse.network.NetworkManagerBle


class ContainerFetcherJvm(private val networkManager: NetworkManagerBle): ContainerFetcher(){

    private val executorService = Executors.newCachedThreadPool()

    private val coroutineCtx  = executorService.asCoroutineDispatcher()

    override suspend fun enqueue(request: ContainerFetcherRequest, listener: ContainerFetcherListener?): Deferred<Int> {
        return GlobalScope.async(coroutineCtx) {
                ContainerDownloaderJobHttpUrlConnection(request, listener, networkManager).download()
        }
    }
}