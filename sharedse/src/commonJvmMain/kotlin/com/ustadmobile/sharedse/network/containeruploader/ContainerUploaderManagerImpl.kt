package com.ustadmobile.sharedse.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.networkmanager.ContainerUploadManager
import com.ustadmobile.sharedse.network.containeruploader.UploadJobRunner
import com.ustadmobile.sharedse.util.LiveDataWorkQueue
import kotlinx.coroutines.CoroutineDispatcher
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_MAIN_COROUTINE_CONTEXT
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class ContainerUploaderManagerImpl(val endpoint: Endpoint, override val di: DI) : ContainerUploadManager(), DIAware {

    private val appDb: UmAppDatabase by on(endpoint).instance(tag = TAG_DB)

    private val mainCoroutineDispatcher: CoroutineDispatcher by di.instance(tag = TAG_MAIN_COROUTINE_CONTEXT)

    init {
        LiveDataWorkQueue(appDb.containerUploadJobDao.findJobs(),
                { item1, item2 -> item1.cujUid == item2.cujUid },
                mainDispatcher = mainCoroutineDispatcher) {

            it.jobStatus = JobStatus.RUNNING
            appDb.containerUploadJobDao.updateStatus(it.jobStatus, it.cujUid)

            val runner = UploadJobRunner(it, endpointUrl = endpoint.url, di = di)
            val status = runner.startUpload()

            it.jobStatus = status
            appDb.containerUploadJobDao.updateStatus(status, it.cujUid)
        }.also { workQueue ->
            GlobalScope.launch {
                workQueue.start()
            }
        }
    }

    override suspend fun enqueue(uploadJobId: Long) {
        appDb.containerUploadJobDao.setStatusToQueueAsync(uploadJobId)
    }

}