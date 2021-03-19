package com.ustadmobile.sharedse.network.containeruploader

import com.ustadmobile.core.network.containeruploader.ContainerUploader2
import com.ustadmobile.core.networkmanager.ContainerUploadManager
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.util.concurrent.Executors
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.networkmanager.ContainerUploaderRequest2

class ContainerUploadManagerCommonJvm(override val di: DI, val siteEndpoint: Endpoint) : ContainerUploadManager(), DIAware {

    private val executorService = Executors.newCachedThreadPool()

    private val coroutineCtx = executorService.asCoroutineDispatcher()

    override suspend fun enqueue(request: ContainerUploaderRequest2): Deferred<Int> {
        return GlobalScope.async(coroutineCtx) {
            ContainerUploader2(request, ContainerUploader2.DEFAULT_CHUNK_SIZE, siteEndpoint, di).upload()
        }
    }

}