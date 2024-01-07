package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.lib.db.entities.ContentEntryImportJob

/**
 * Use case that is invoked when the ContentEntry has been saved to the database and content itself
 * needs to be imported. The use case will start the process.
 *
 * On Android/JVM: Uses the platform scheduler (e.g. WorkManager/Quartz) to run a job.
 * On JS: sends request to the server, which in turn uses quartz on JVM as above
 */
interface EnqueueContentEntryImportUseCase {

    /**
     * Run the given ContentJob / ContentJobItem. The ContentJobItem's contentEntryUid MUST be set
     * to the related ContentEntry before invoking the function
     */
    suspend operator fun invoke(
        contentJobItem: ContentEntryImportJob,
    )

}