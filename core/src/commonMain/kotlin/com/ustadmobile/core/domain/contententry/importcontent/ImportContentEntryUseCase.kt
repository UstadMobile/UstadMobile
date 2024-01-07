package com.ustadmobile.core.domain.contententry.importcontent

/**
 * Implementations will use the platform scheduler (e.g. WorkManager on Android, Quartz on JVM) to
 * run this UseCase. This does not run on the browser.
 *
 * It will use
 *
 * On Desktop/Android: will use EnqueueBlobUploadClientUseCase to upload the content to the server.
 *
 */
interface ImportContentEntryUseCase {

    suspend operator fun invoke(
        contentEntryImportJobId: Long,
    )

}