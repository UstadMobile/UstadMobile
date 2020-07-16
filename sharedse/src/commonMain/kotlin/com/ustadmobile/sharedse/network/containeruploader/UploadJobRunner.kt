package com.ustadmobile.sharedse.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcher
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class UploadJobRunner(private val endpointUrl: String, override val di: DI) : DIAware {

    private val db: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = UmAppDatabase.TAG_DB)

    private val containerUploader: ContainerUploader by instance()

    private val currentUploadAttempt = atomic(null as Deferred<Int>?)

    /**
     * Lock used for any operation that is changing the status of the Fetch download
     */
    private val downloadStatusLock = Mutex()

    suspend fun startUpload() {


        val listOfFiles: List<ContainerEntryWithMd5> = listOf()


        val m5sServerDoesntHave = "1;2;3"

        val request = ContainerUploaderRequest(m5sServerDoesntHave,
                "", endpointUrl)

        var jobDeferred: Deferred<Int>? = null
        downloadStatusLock.withLock {
            jobDeferred = containerUploader.enqueue(request, object :  AbstractContainerUploaderListener() {
                override fun onProgress(request: ContainerUploaderRequest, bytesUploaded: Long, contentLength: Long) {
                    super.onProgress(request, bytesUploaded, contentLength)
                }
            })
            currentUploadAttempt.value = jobDeferred
        }


        /*  val containerEntryWithFileList = db.someDao.findCotnainerEntryWithContainerEntryFile(containerUid)

          val md5sServerDoesntHaveYet = httpClient.post(containerEntryWithFileList.map  { it.md5sum} )

          val itemsToUpload = containerEntryWithFileList.filter { it.md5 in md5sServerDoesntHaveYet}

  */


    }


}