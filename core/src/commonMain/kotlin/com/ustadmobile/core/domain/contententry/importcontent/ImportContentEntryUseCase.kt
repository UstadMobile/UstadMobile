package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.contentformats.ContentImportersManager
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

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
    @Suppress("unused") //will be used - design in progress
    private val enqueueBlobUploadClientUseCase: EnqueueBlobUploadClientUseCase? = null,
    private val httpClient: HttpClient? = null,
) {

    suspend operator fun invoke(
        contentEntryImportJobId: Long,
    ): ContentEntryVersion {
        val job = db.contentEntryImportJobDao
            .findByUidAsync(contentEntryImportJobId) ?: throw IllegalArgumentException(
                "$contentEntryImportJobId not found in db")

        //In future should handle situation where importer is not already specified.
        val importer = importersManager.requireImporterById(job.cjiPluginId)

        val contentEntryVersionEntity = importer.importContent(
            jobItem = job,
            progressListener = {

            }
        )

        db.contentEntryVersionDao.insertAsync(contentEntryVersionEntity)

        val enqueueBlobUploadClientUseCaseVal = enqueueBlobUploadClientUseCase
        if(enqueueBlobUploadClientUseCaseVal != null && httpClient != null) {
            //Because the entry was imported just now, it will be in the cache. This will still
            //work offline.
            val manifestUrl = contentEntryVersionEntity.cevSitemapUrl
                ?: throw IllegalStateException("imported entry has no manifest url")
            val manifest: ContentManifest = httpClient.get(manifestUrl).body()

            enqueueBlobUploadClientUseCaseVal(
                items = manifest.entries.map {
                    EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem(
                        blobUrl = it.bodyDataUrl,
                        tableId = ContentEntryVersion.TABLE_ID,
                        entityUid = contentEntryVersionEntity.cevUid,
                        retentionLockIdToRelease = 0,//Can create retention lock before
                    )
                } + EnqueueBlobUploadClientUseCase.EnqueueBlobUploadItem(
                    blobUrl = manifestUrl,
                    tableId = ContentEntryVersion.TABLE_ID,
                    entityUid = contentEntryVersionEntity.cevUid,
                    retentionLockIdToRelease = 0,
                ),
                batchUuid = randomUuidAsString()
            )
        }

        return contentEntryVersionEntity
    }

}