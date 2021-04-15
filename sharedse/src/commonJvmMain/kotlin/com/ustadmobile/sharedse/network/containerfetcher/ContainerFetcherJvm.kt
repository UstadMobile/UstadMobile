package com.ustadmobile.sharedse.network.containerfetcher

import kotlinx.coroutines.*
import java.util.concurrent.*
import org.kodein.di.*
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherRequest2
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherListener2
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherJobOkHttp

class ContainerFetcherJvm(override val di: DI): ContainerFetcher(), DIAware{

    private val executorService = Executors.newCachedThreadPool()

    private val coroutineCtx  = executorService.asCoroutineDispatcher()

    override suspend fun enqueue(request: ContainerFetcherRequest2, listener: ContainerFetcherListener2?): Deferred<Int> {
        return GlobalScope.async(coroutineCtx) {
            ContainerFetcherJobOkHttp(request, listener, di).download()
        }
    }
}