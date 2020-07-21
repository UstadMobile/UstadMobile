package com.ustadmobile.sharedse.network.containerfetcher

import kotlinx.coroutines.*
import java.util.concurrent.*
import com.ustadmobile.sharedse.network.NetworkManagerBle
import org.kodein.di.*


class ContainerFetcherJvm(override val di: DI): ContainerFetcher(), DIAware{

    private val executorService = Executors.newCachedThreadPool()

    private val coroutineCtx  = executorService.asCoroutineDispatcher()

    private val networkManager: NetworkManagerBle by di.instance()

    override suspend fun enqueue(request: ContainerFetcherRequest, listener: ContainerFetcherListener?): Deferred<Int> {
        return GlobalScope.async(coroutineCtx) {
            ContainerFetcherJobHttpUrlConnection(request, listener, di).download()
        }
    }
}