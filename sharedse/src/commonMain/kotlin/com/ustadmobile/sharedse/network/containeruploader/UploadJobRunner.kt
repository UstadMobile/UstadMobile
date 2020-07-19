package com.ustadmobile.sharedse.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.lib.db.entities.ContainerUploadJob
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcher
import io.ktor.client.request.parameter
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

        val containerEntryWithFileList =
                db.containerEntryDao.findByContainer(containerUploadJob.cujContainerUid)

        val listOfMd5Sum = containerEntryWithFileList.map { it.containerEntryFile?.cefMd5 }
        val listOfM5SumStr = listOfMd5Sum.joinToString(";")

        val m5sServerDoesntHave = currentHttpClient.post<List<String>> {
            url("$endpointUrl/ContainerUpload/checkExistingMd5/$listOfM5SumStr")
        }

        val itemsToUpload = listOfMd5Sum.filter { it in m5sServerDoesntHave }.joinToString(";")

        val request = ContainerUploaderRequest(itemsToUpload,
                "$endpointUrl/upload/", endpointUrl)

        var uploadAttemptStatus = -1
        var jobDeferred: Deferred<Int>? = null
        downloadStatusLock.withLock {
            jobDeferred = containerUploader.enqueue(request, object : AbstractContainerUploaderListener() {
                override fun onProgress(request: ContainerUploaderRequest, bytesUploaded: Long, contentLength: Long) {
                    super.onProgress(request, bytesUploaded, contentLength)
                }
            })
            currentUploadAttempt.value = jobDeferred
        }
        uploadAttemptStatus = jobDeferred?.await() ?: JobStatus.FAILED


    }


}