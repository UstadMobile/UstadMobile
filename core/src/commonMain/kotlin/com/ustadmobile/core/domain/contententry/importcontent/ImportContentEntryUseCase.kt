package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.contentformats.ContentImportersManager
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Implementations will use the platform scheduler (e.g. WorkManager on Android, Quartz on JVM) to
 * run this UseCase. This does not run on the browser.
 *
 * It will use
 *
 * On Desktop/Android: will use EnqueueBlobUploadClientUseCase to upload the content to the server.
 *
 */
class ImportContentEntryUseCase(
    private val db: UmAppDatabase,
    private val importersManager: ContentImportersManager,
    private val json: Json,
    private val enqueueBlobUploadClientUseCase: EnqueueBlobUploadClientUseCase? = null,
    private val createRetentionLocksForManifestUseCase: CreateRetentionLocksForManifestUseCase? = null,
    private val httpClient: HttpClient? = null,
) {

    suspend operator fun invoke(
        contentEntryImportJobId: Long,
    ): ContentEntryVersion {
        val job = db.contentEntryImportJobDao()
            .findByUidAsync(contentEntryImportJobId) ?: throw IllegalArgumentException(
                "$contentEntryImportJobId not found in db")

        //In future should handle situation where importer is not already specified.
        val importer = importersManager.requireImporterById(job.cjiPluginId)

        var latestJobStatus: ContentEntryImportJob? = null

        val contentEntryVersionEntity =  try {
            coroutineScope {
                val updaterJob = launch {
                    while(isActive) {
                        Napier.v { "CompressVideo: update status = $latestJobStatus" }
                        latestJobStatus?.also { jobVal ->
                            db.contentEntryImportJobDao().updateItemProgress(
                                cjiUid = jobVal.cjiUid,
                                cjiProgress = jobVal.cjiItemProgress,
                                cjiTotal = jobVal.cjiItemTotal,
                            )
                        }

                        delay(PROGRESS_UPDATE_INTERVAL)
                    }
                }

                db.contentEntryImportJobDao().updateItemStatus(
                    cjiUid = job.cjiUid,
                    status = JobStatus.RUNNING
                )

                importer.importContent(
                    jobItem = job,
                    progressListener = {
                        latestJobStatus = it
                    }
                ).also {
                    db.contentEntryImportJobDao().updateItemStatus(
                        cjiUid = job.cjiUid,
                        status = JobStatus.COMPLETE
                    )
                    updaterJob.cancel()
                }
            }
        }catch(e: Throwable) {
            withContext(NonCancellable) {
                db.contentEntryImportJobDao().updateItemStatusAndError(
                    cjiUid = job.cjiUid,
                    status = if(e is CancellationException) {
                        JobStatus.CANCELED
                    }else {
                        JobStatus.FAILED
                    },
                    error = if(e !is CancellationException) e.message else null
                )
            }

            throw e
        }

        db.contentEntryVersionDao().insertAsync(contentEntryVersionEntity)

        val enqueueBlobUploadClientUseCaseVal = enqueueBlobUploadClientUseCase
        if(enqueueBlobUploadClientUseCaseVal != null && httpClient != null) {
            //Because the entry was imported just now, it will be in the cache. This will still
            //work offline.
            val manifestUrl = contentEntryVersionEntity.cevManifestUrl
                ?: throw IllegalStateException("imported entry has no manifest url")
            val manifest: ContentManifest = json.decodeFromString(
                httpClient.get(manifestUrl).bodyAsDecodedText())
            val locksCreated = createRetentionLocksForManifestUseCase?.invoke(
                contentEntryVersionUid = contentEntryVersionEntity.cevLct,
                manifestUrl = manifestUrl,
                manifest = manifest
            )?.associate { it.url to it.lockId } ?: emptyMap()

            /*
             * It is possible that multiple entries in the same manifest can have the same SHA-256
             * sum, and therefor, would have the same blob url. Duplicates need to be eliminated
             * (using .distinctBy )
             */
            val entriesToUpload = manifest.entries.map {
                EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem(
                    blobUrl = it.bodyDataUrl,
                    tableId = ContentEntryVersion.TABLE_ID,
                    entityUid = contentEntryVersionEntity.cevUid,
                    retentionLockIdToRelease = locksCreated[it.bodyDataUrl] ?: 0,
                )
            }.distinctBy { it.blobUrl } + EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem(
                blobUrl = manifestUrl,
                tableId = ContentEntryVersion.TABLE_ID,
                entityUid = contentEntryVersionEntity.cevUid,
                retentionLockIdToRelease = locksCreated[manifestUrl] ?: 0,
            )

            enqueueBlobUploadClientUseCaseVal(
                items = entriesToUpload,
                batchUuid = randomUuidAsString(),
                tableId = ContentEntryVersion.TABLE_ID,
                entityUid = contentEntryVersionEntity.cevUid
            )
        }

        return contentEntryVersionEntity
    }

    companion object {

        const val PROGRESS_UPDATE_INTERVAL = 1_000L

    }

}