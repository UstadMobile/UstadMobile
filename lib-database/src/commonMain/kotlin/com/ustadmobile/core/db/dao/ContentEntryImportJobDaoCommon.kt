package com.ustadmobile.core.db.dao

import com.ustadmobile.core.db.JobStatus

object ContentEntryImportJobDaoCommon {

    const val FIND_IN_PROGRESS_JOBS_BY_CONTENT_ENTRY_UID = """
        SELECT ContentEntryImportJob.cjiUid,
               ContentEntryImportJob.cjiItemProgress,
               ContentEntryImportJob.cjiItemTotal,
               ContentEntryImportJob.cjiStatus,
               ContentEntryImportJob.cjiError,
               ContentEntryImportJob.cjiOwnerPersonUid
          FROM ContentEntryImportJob
         WHERE ContentEntryImportJob.cjiContentEntryUid = :contentEntryUid
           AND (   ContentEntryImportJob.cjiStatus BETWEEN ${JobStatus.QUEUED} AND ${JobStatus.RUNNING_MAX}
                OR (ContentEntryImportJob.cjiStatus = ${JobStatus.FAILED} AND NOT ContentEntryImportJob.cjiErrorDismissed))
    """

}