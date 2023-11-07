package com.ustadmobile.core.domain.contententry.import

import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem

/**
 * Use case that is invoked when the ContentEntry has been saved to the database and content itself
 * needs to be imported. The use case will start the process.
 *
 * On Android: saves contentJob and contentJobItem to database, then uses WorkManager to run the job
 * On JVM: saves contentJob and contentJobItem to database, then uses Quartz to run the job
 * On JS: sends request to the server
 */
interface ImportContentUseCase {

    /**
     * Run the given ContentJob / ContentJobItem. The ContentJobItem's contentEntryUid MUST be set
     * to the related ContentEntry before invoking the function
     */
    suspend operator fun invoke(
        contentJob: ContentJob,
        contentJobItem: ContentJobItem,
    )

}