package com.ustadmobile.sharedse.network.containeruploader

import com.ustadmobile.core.networkmanager.ContainerUploaderCommon
import com.ustadmobile.core.networkmanager.ContainerUploaderRequest
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.util.concurrent.Executors

class ContainerUploaderCommonJvm(override val di: DI) : ContainerUploaderCommon(), DIAware {

    private val executorService = Executors.newCachedThreadPool()

    private val coroutineCtx = executorService.asCoroutineDispatcher()

    override suspend fun enqueue(request: ContainerUploaderRequest): Deferred<Int> {
        return GlobalScope.async(coroutineCtx) {
            ContainerUploader(request, di = di).upload()
        }
    }

}