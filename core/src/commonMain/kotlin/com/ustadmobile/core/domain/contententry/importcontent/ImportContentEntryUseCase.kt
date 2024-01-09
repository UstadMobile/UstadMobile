package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.core.contentformats.ContentImportersManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase

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
) {

    suspend operator fun invoke(
        contentEntryImportJobId: Long,
    ) {
        val job = db.contentEntryImportJobDao
            .findByUidAsync(contentEntryImportJobId) ?: throw IllegalArgumentException(
                "$contentEntryImportJobId not found in db")

        //HERE - Should handle situation where importer is not already specified.
        val importer = importersManager.requireImporterById(job.cjiPluginId)

        val contentEntryVersionEntity = importer.importContent(
            jobItem = job,
            progressListener = {

            }
        )

        db.contentEntryVersionDao.insertAsync(contentEntryVersionEntity)
    }

}