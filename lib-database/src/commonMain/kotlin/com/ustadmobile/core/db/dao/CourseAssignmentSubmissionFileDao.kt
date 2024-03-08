package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionFile
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class CourseAssignmentSubmissionFileDao : BaseDao<CourseAssignmentSubmissionFile> {


    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<CourseAssignmentSubmissionFile>)


    @Query("""
        SELECT CourseAssignmentSubmissionFile.*
          FROM CourseAssignmentSubmissionFile 
         WHERE CourseAssignmentSubmissionFile.casaSubmissionUid = 
               COALESCE((SELECT CourseAssignmentSubmission.casUid
                           FROM CourseAssignmentSubmission
                          WHERE casSubmitterUid = (${ClazzAssignmentDaoCommon.SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL})
                       ORDER BY casTimestamp DESC
                          LIMIT 1), -1)
    """)
    abstract suspend fun getLatestSubmissionAttachmentsForUserAsync(
        accountPersonUid: Long,
        assignmentUid: Long
    ): List<CourseAssignmentSubmissionFile>


}