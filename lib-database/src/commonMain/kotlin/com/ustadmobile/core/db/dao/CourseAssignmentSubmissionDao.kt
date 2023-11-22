package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL
import app.cash.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class CourseAssignmentSubmissionDao : BaseDao<CourseAssignmentSubmission> {

    @Insert
    abstract suspend fun insertListAsync(entityList: List<CourseAssignmentSubmission>)


    @Query("""
        SELECT * 
          FROM CourseAssignmentSubmission
          
               LEFT JOIN CourseAssignmentSubmissionAttachment
               ON CourseAssignmentSubmissionAttachment.casaSubmissionUid = CourseAssignmentSubmission.casUid
               
         WHERE casAssignmentUid = :assignmentUid
           AND casSubmitterUid = :submitterUid
      ORDER BY casTimestamp DESC
    """)
    abstract fun getAllSubmissionsFromSubmitter(assignmentUid: Long, submitterUid: Long)
            : PagingSource<Int, CourseAssignmentSubmissionWithAttachment>

    @Query("""
         SELECT CourseAssignmentSubmission.*, CourseAssignmentSubmissionAttachment.*
          FROM CourseAssignmentSubmission
               LEFT JOIN CourseAssignmentSubmissionAttachment
                    ON CourseAssignmentSubmissionAttachment.casaSubmissionUid = CourseAssignmentSubmission.casUid
         WHERE casSubmitterUid = ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL)
    """)
    abstract fun getAllSubmissionsForUser(
        accountPersonUid: Long,
        assignmentUid: Long,
    ): Flow<List<CourseAssignmentSubmissionWithAttachment>>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT CourseAssignmentSubmission.*
          FROM CourseAssignmentSubmission
         WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
           AND CourseAssignmentSubmission.casSubmitterUid = :submitterUid
      ORDER BY CourseAssignmentSubmission.casTimestamp DESC      
    """)
    abstract fun getAllSubmissionsFromSubmitterAsFlow(
        submitterUid: Long,
        assignmentUid: Long,
    ): Flow<List<CourseAssignmentSubmission>>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("getLatestSubmissionForUserAsync"),
            //Get the ClazzEnrolment objects to correctly determine if active person is student
            HttpServerFunctionCall(
                functionDao = ClazzAssignmentDao::class,
                functionName = "findEnrolmentsByPersonUidAndAssignmentUid"
            ),
            //Get the CourseGroupMember entities that apply for this assignment and personUid if any
            HttpServerFunctionCall(
                functionDao = ClazzAssignmentDao::class,
                functionName = "findCourseGroupMembersByPersonUidAndAssignmentUid"),
            //PeerReviewerAllocations if required
            HttpServerFunctionCall(
                functionDao = ClazzAssignmentDao::class,
                functionName = "findPeerReviewerAllocationsByPersonUidAndAssignmentUid",
            ),
        )
    )
    @Query("""
        SELECT CourseAssignmentSubmission.*
          FROM CourseAssignmentSubmission
         WHERE casSubmitterUid = ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL)
      ORDER BY casTimestamp DESC
         LIMIT 1
    """)
    abstract suspend fun getLatestSubmissionForUserAsync(
        accountPersonUid: Long,
        assignmentUid: Long
    ): CourseAssignmentSubmission?

    @Query("""
        SELECT EXISTS
               (SELECT CourseAssignmentSubmission.casUid
                  FROM CourseAssignmentSubmission
                 WHERE casSubmitterUid = ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL))
    """)
    abstract suspend fun doesUserHaveSubmissions(
        accountPersonUid: Long,
        assignmentUid: Long,
    ): Boolean


    @Query("""
        SELECT Count(casUid)
          FROM CourseAssignmentSubmission
         WHERE casAssignmentUid = :assignmentUid
           AND casSubmitterUid = :submitterUid
           AND casType = ${CourseAssignmentSubmission.SUBMISSION_TYPE_FILE}
    """)
    abstract suspend fun countFileSubmissionFromStudent(assignmentUid: Long, submitterUid: Long): Int

    @Query("""
        SELECT Count(casUid)
          FROM CourseAssignmentSubmission
         WHERE casAssignmentUid = :assignmentUid
           AND casSubmitterUid = :submitterUid
    """)
    abstract suspend fun countSubmissionsFromSubmitter(assignmentUid: Long, submitterUid: Long): Int

    @Query("""
           SELECT COALESCE((
                SELECT (CASE WHEN CourseAssignmentMark.camAssignmentUid IS NOT NULL 
                             THEN ${CourseAssignmentSubmission.MARKED}
                             ELSE ${CourseAssignmentSubmission.SUBMITTED} 
                             END) AS status
                  FROM CourseAssignmentSubmission
                       
                       LEFT JOIN CourseAssignmentMark
                       ON CourseAssignmentMark.camAssignmentUid = :assignmentUid
                       AND CourseAssignmentMark.camSubmitterUid = :submitterUid
                       
                 WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                   AND CourseAssignmentSubmission.casSubmitterUid = :submitterUid
                 LIMIT 1
           ),${CourseAssignmentSubmission.NOT_SUBMITTED}) AS Status
    """)
    abstract fun getStatusOfAssignmentForSubmitter(assignmentUid: Long, submitterUid: Long): Flow<Int>

    @Query("""
        SELECT * 
          FROM CourseAssignmentSubmission
         WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
           AND CourseAssignmentSubmission.casSubmitterUid = :submitterUid
      ORDER BY casTimestamp DESC
         LIMIT 1
    """)
    abstract suspend fun findLastSubmissionFromStudent(submitterUid: Long, assignmentUid: Long): CourseAssignmentSubmission?


    @Query("""
         SELECT NOT EXISTS(SELECT 1
                        FROM CourseAssignmentSubmission
                       WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                       LIMIT 1)
    """)
    abstract fun checkNoSubmissionsMade(assignmentUid: Long): Flow<Boolean>

    @Query("""
         SELECT NOT EXISTS(SELECT 1
                        FROM CourseAssignmentSubmission
                       WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                       LIMIT 1)
    """)
    abstract suspend fun checkNoSubmissionsMadeAsync(assignmentUid: Long): Boolean


    @Query("""
         SELECT NOT EXISTS(SELECT 1
                        FROM CourseAssignmentSubmission
                       WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                       LIMIT 1)
    """)
    abstract fun checkNoSubmissionsMadeFlow(assignmentUid: Long): Flow<Boolean>

}