package com.ustadmobile.sharedse.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.db.entities.ContainerUploadJob
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpStatement
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class UploadJobRunner(private val containerUploadJob: ContainerUploadJob, private val endpointUrl: String, override val di: DI) : DIAware {

    private val db: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = UmAppDatabase.TAG_DB)

    private val containerUploader: ContainerUploader by instance()

    private val currentUploadAttempt = atomic(null as Deferred<Int>?)

    private val currentHttpClient = defaultHttpClient()

    /**
     * Lock used for any operation that is changing the status of the Fetch download
     */
    private val downloadStatusLock = Mutex()

    suspend fun startUpload() {

        val containerEntryWithFileList = db.containerEntryDao
                .findByContainer(containerUploadJob.cujContainerUid)

        var containerEntryUidList = containerUploadJob.containerEntryFileUids
        val md5ListToUpload: String
        if (containerEntryUidList.isNullOrEmpty()) {

            val listOfMd5SumStr = containerEntryWithFileList.map { it.containerEntryFile?.cefMd5 }
                    .joinToString(";")

            val md5sServerDoesntHave = currentHttpClient.post<List<String>> {
                url(UMFileUtil.joinPaths(endpointUrl, "/ContainerUpload/checkExistingMd5/", listOfMd5SumStr))
            }

            val containerEntriesServerDoesntHave = containerEntryWithFileList
                    .filter { it.containerEntryFile?.cefMd5 in md5sServerDoesntHave }

            containerEntryUidList = containerEntriesServerDoesntHave.map { it.containerEntryFile?.cefUid }
                    .joinToString(",")

            containerUploadJob.containerEntryFileUids = containerEntryUidList
            db.containerUploadJobDao.update(containerUploadJob)

            md5ListToUpload = containerEntriesServerDoesntHave.map { it.containerEntryFile?.cefMd5 }.joinToString(";")

        } else {
            md5ListToUpload = db.containerEntryFileDao.findEntriesByUids(
                    containerEntryUidList.split(";").map { it.toLong() })
                    .map { it.cefMd5 }.joinToString(";")
        }

        val request = ContainerUploaderRequest(containerUploadJob.cujUid,
                md5ListToUpload, "$endpointUrl/upload/", endpointUrl)

        var uploadAttemptStatus = -1
        var jobDeferred: Deferred<Int>? = null

        var attemptNum = 0
        while (attemptNum++ < 3) {

            downloadStatusLock.withLock {

                jobDeferred = containerUploader.enqueue(request, object : AbstractContainerUploaderListener() {
                    override fun onProgress(request: ContainerUploaderRequest, bytesUploaded: Long, contentLength: Long) {
                        super.onProgress(request, bytesUploaded, contentLength)

                    }
                })
                currentUploadAttempt.value = jobDeferred

            }
            uploadAttemptStatus = jobDeferred?.await() ?: JobStatus.FAILED

            if (uploadAttemptStatus == JobStatus.COMPLETE) {
                break
            }
        }

        val containerEntries = db.containerEntryDao.findByContainerWithMd5(containerUploadJob.cujContainerUid)

        if (uploadAttemptStatus == JobStatus.COMPLETE) {

            val container = db.containerDao.findByUid(containerUploadJob.cujContainerUid)

            val job = db.containerUploadJobDao.findByUid(containerUploadJob.cujUid)
            currentHttpClient.post<HttpStatement>() {
                url(UMFileUtil.joinPaths(endpointUrl, "/upload/sessionId/${job.sessionId}/"))
                header("content-type","application/json")
                body = containerEntries
            }.execute()


        }

    }


}