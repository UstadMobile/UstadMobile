package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionFile
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class CourseAssignmentSubmissionFileDao : BaseDao<CourseAssignmentSubmissionFile> {


    @Insert
    abstract suspend fun insertListAsync(entityList: List<CourseAssignmentSubmissionFile>)

    @Query("""
        SELECT CourseAssignmentSubmissionFile.*, TransferJobItem.*
          FROM CourseAssignmentSubmissionFile
               LEFT JOIN TransferJobItem
                         ON TransferJobItem.tjiEntityUid = CourseAssignmentSubmissionFile.casaUid
                            AND TransferJobItem.tjiTableId = ${CourseAssignmentSubmissionFile.TABLE_ID}
         WHERE CourseAssignmentSubmissionFile.casaSubmissionUid = :submissionUid
           AND NOT CourseAssignmentSubmissionFile.casaDeleted
    """)
    abstract fun getBySubmissionUid(
        submissionUid: Long
    ): Flow<List<CourseAssignmentSubmissionFileAndTransferJob>>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT CourseAssignmentSubmissionFile.*, TransferJobItem.*
          FROM CourseAssignmentSubmissionFile
               LEFT JOIN TransferJobItem
                         ON TransferJobItem.tjiEntityUid = CourseAssignmentSubmissionFile.casaUid
                            AND TransferJobItem.tjiTableId = ${CourseAssignmentSubmissionFile.TABLE_ID}
         WHERE CourseAssignmentSubmissionFile.casaSubmissionUid IN
               (SELECT CourseAssignmentSubmission.casUid
                  FROM CourseAssignmentSubmission
                 WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                   AND CourseAssignmentSubmission.casSubmitterUid = 
                       (${ClazzAssignmentDaoCommon.SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL}))
           AND NOT CourseAssignmentSubmissionFile.casaDeleted        
    """)
    abstract fun getByAssignmentUidAndPersonUid(
        accountPersonUid: Long,
        assignmentUid: Long,
    ): Flow<List<CourseAssignmentSubmissionFileAndTransferJob>>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("""
        SELECT CourseAssignmentSubmissionFile.*, TransferJobItem.*
          FROM CourseAssignmentSubmissionFile
               LEFT JOIN TransferJobItem
                         ON TransferJobItem.tjiEntityUid = CourseAssignmentSubmissionFile.casaUid
                            AND TransferJobItem.tjiTableId = ${CourseAssignmentSubmissionFile.TABLE_ID}
         WHERE CourseAssignmentSubmissionFile.casaSubmitterUid = :submitterUid
           AND CourseAssignmentSubmissionFile.casaCaUid = :assignmentUid
           AND NOT CourseAssignmentSubmissionFile.casaDeleted
    """)
    abstract fun getAllSubmissionFilesFromSubmitterAsFlow(
        submitterUid: Long,
        assignmentUid: Long,
    ): Flow<List<CourseAssignmentSubmissionFileAndTransferJob>>

    @Query("""
        UPDATE CourseAssignmentSubmissionFile
           SET casaUri = :uri,
               casaTimestamp = :updateTime
         WHERE casaUid = :casaUid
    """)
    abstract suspend fun updateUri(
        casaUid: Long,
        uri: String,
        updateTime: Long,
    )


    @Query("""
        UPDATE CourseAssignmentSubmissionFile
           SET casaDeleted = :deleted,
               casaTimestamp = :updateTime
         WHERE casaUid = :casaUid
    """)
    abstract suspend fun setDeleted(
        casaUid: Long,
        deleted: Boolean,
        updateTime: Long,
    )

}