package com.ustadmobile.sharedse.network

import com.ustadmobile.sharedse.network.containeruploader.ContainerUploaderListener
import com.ustadmobile.sharedse.network.containeruploader.ContainerUploaderRequest
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.util.concurrent.Executors
import com.ustadmobile.sharedse.network.containeruploader.ContainerUploader

class ContainerUplaoderJvm(override val di: DI) : ContainerUploader(), DIAware {

    private val executorService = Executors.newCachedThreadPool()

    private val coroutineCtx = executorService.asCoroutineDispatcher()

    private val networkManager: NetworkManagerBle by di.instance()

    override suspend fun enqueue(request: ContainerUploaderRequest, listener: ContainerUploaderListener?): Deferred<Int> {
        return GlobalScope.async(coroutineCtx) {
            ContainerUploader(request, listener, di = di).upload()
        }
    }

}