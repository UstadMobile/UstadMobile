package com.ustadmobile.sharedse.network

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.networkmanager.ContainerUploadManager
import com.ustadmobile.sharedse.network.containeruploader.UploadJobRunner
import com.ustadmobile.sharedse.util.LiveDataWorkQueue
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class ContainerUploaderManagerImp(val endpoint: Endpoint, override val di: DI) : ContainerUploadManager(), DIAware {

    private val appDb: UmAppDatabase by on(endpoint).instance(tag = TAG_DB)

    override suspend fun enqueue(uploadJobId: Long) {
        appDb.containerUploadJobDao.setStatusToQueue(uploadJobId)

        LiveDataWorkQueue(appDb.containerUploadJobDao.findJobs(),
                { item1, item2 -> item1.cujUid == item2.cujUid }) {

            it.jobStatus = JobStatus.RUNNING
            appDb.containerUploadJobDao.update(it)

            val runner = UploadJobRunner(it, endpoint.url, di)
            val status = runner.startUpload()

            it.jobStatus = status
            appDb.containerUploadJobDao.update(it)
        }.start()
    }

}