package com.ustadmobile.core.networkmanager

import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.toContainerEntryWithMd5
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.UMUUID
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerImportJob
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

class ImportJobRunner(private val containerImportJob: ContainerImportJob, private val retryDelay: Long = DEFAULT_RETRY_DELAY, private val endpointUrl: String, override val di: DI) : DIAware {

    private val db: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = UmAppDatabase.TAG_DB)

    private val containerUploader: ContainerUploadManager by on(Endpoint(endpointUrl)).instance()

    private val contentImportManager: ContentImportManager by on(Endpoint(endpointUrl)).instance()

    private val containerManager: ContainerDownloadManager by on(Endpoint(endpointUrl)).instance()

    private val _connectivityStatus = DoorMutableLiveData<ConnectivityStatus>()

    val connectivityStatus: DoorLiveData<ConnectivityStatus>
        get() = _connectivityStatus

    private val importProgress = atomic(0L)

    private val IMPORT_RUNNER_TAG = "ImportJobRunner"


    suspend fun progressUpdater() = coroutineScope {
        while (isActive) {
            Napier.d(tag = IMPORT_RUNNER_TAG, message = "progress updating at value ${importProgress.value}")
            db.containerImportJobDao.updateProgress(importProgress.value, 100, containerImportJob.cijUid)
            delay(1000L)
        }
    }

    suspend fun importContainer(markContainerAsDownloaded: Boolean = false) {

        val filePathWithoutPrefix = if (markContainerAsDownloaded)
            containerImportJob.cijFilePath?.removePrefix("file://")
        else
            containerImportJob.cijFilePath

        val filePath = filePathWithoutPrefix ?: throw IllegalArgumentException("filePath not given")
        val mimeType = containerImportJob.cijMimeType
                ?: throw IllegalArgumentException("mimeType not given")
        val containerBaseDir = containerImportJob.cijContainerBaseDir
                ?: throw IllegalArgumentException("container folder not given")
        val contentEntryUid = containerImportJob.cijContentEntryUid.takeIf { it != 0L }
                ?: throw IllegalArgumentException("contentEntryUid not given")
        val params = containerImportJob.cijConversionParams
        var conversionParams: Map<String, String> = mapOf()
        if(params != null){
            conversionParams = Json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), params)
        }

        val importProgressUpdateJob = GlobalScope.async { progressUpdater() }
        var container: Container? = null
        try {
            container = contentImportManager.importFileToContainer(
                    filePath,
                    mimeType,
                    contentEntryUid,
                    containerBaseDir, conversionParams) {
                importProgress.value = it.toLong()
            }
        } catch (e: Exception) {
            Napier.e(tag = IMPORT_RUNNER_TAG, throwable = e, message = e.message?: "")
            throw e
        } finally {
            Napier.d(tag = IMPORT_RUNNER_TAG, message = "cancelled importJob")
            importProgressUpdateJob.cancel()
        }

        if (container == null) {
            return
        }

        containerImportJob.cijContainerUid = container.containerUid
        db.containerImportJobDao.updateImportComplete(true, container.containerUid,
                containerImportJob.cijUid)

        if (markContainerAsDownloaded) {
            containerManager.handleContainerLocalImport(container)
        }
    }

    suspend fun upload(): Int {

        var attemptNum = 0
        var uploadAttemptStatus = JobStatus.FAILED
        while (uploadAttemptStatus != JobStatus.COMPLETE && attemptNum++ < 3) {
            try {
                val containerEntries = db.containerEntryDao.findByContainer(
                        containerImportJob.cijContainerUid)

                var uploadSessionId = containerImportJob.cijSessionId
                if(uploadSessionId == null) {
                    uploadSessionId = UMUUID.randomUUID().toString()
                    containerImportJob.cijSessionId = uploadSessionId
                    db.containerImportJobDao.updateSessionId(containerImportJob.cijContainerUid,
                        uploadSessionId)
                }

                val containerUploaderRequest2 = ContainerUploaderRequest2(uploadSessionId,
                    containerEntries.map { it.toContainerEntryWithMd5() }, endpointUrl)
                uploadAttemptStatus = containerUploader.enqueue(containerUploaderRequest2).await()
            } catch (e: Exception) {
                Napier.e("ImportJobRunner: ${containerImportJob.cijUid} - exception", e)

                val status = connectivityStatus.getValue()
                val connectivityState = status?.connectivityState
                if (connectivityState != ConnectivityStatus.STATE_UNMETERED && connectivityState != ConnectivityStatus.STATE_METERED) {
                    return JobStatus.QUEUED
                }

                delay(retryDelay)
            }
        }

        return uploadAttemptStatus

    }


    companion object {
        const val DEFAULT_RETRY_DELAY = 1000L
    }


}