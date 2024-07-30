package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.MAX_VALID_DATE
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.ASSIGNMENT_CLAZZ_UID_CTE_SQL
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SUBMITTER_LIST_WITHOUT_ASSIGNMENT_CTE
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.HAS_LEARNINGRECORD_AND_MEMBER_VIEW_PERMISSION_CTE_SQL
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SORT_NAME_ASC
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SORT_NAME_DESC
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SUBMITTER_LIST_CTE2_SQL
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SELECT_ASSIGNMENT_IS_PEERMARKED_SQL
import com.ustadmobile.lib.db.composites.AssignmentSubmitterUidAndName
import com.ustadmobile.lib.db.composites.ClazzAssignmentAndBlock
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission.Companion.MIN_SUBMITTER_UID_FOR_PERSON


@DoorDao
@Repository
expect abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment>, OneToManyJoinDao<ClazzAssignment> {

    @Query("""
        SELECT * 
          FROM ClazzAssignment
         WHERE ClazzAssignment.caClazzUid = :clazzUid
    """)
    abstract suspend fun getAllAssignmentsByClazzUidAsync(clazzUid: Long): List<ClazzAssignment>


    @Query("""
        UPDATE ClazzAssignment 
           SET caActive = :active, 
               caLct = :changeTime
         WHERE caUid = :cbUid""")
    abstract suspend fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)



    @Query("""
        WITH $HAS_LEARNINGRECORD_AND_MEMBER_VIEW_PERMISSION_CTE_SQL,
        $ASSIGNMENT_CLAZZ_UID_CTE_SQL,
        $SUBMITTER_LIST_CTE2_SQL
        
        SELECT 
              -- whether or not the active user has permission to view learner records 
              (SELECT hasPermission
                 FROM HasLearningRecordSelectPermission) AS activeUserHasViewLearnerRecordsPermission,
        
              (SELECT COUNT(*)
                 FROM SubmitterList) AS totalStudents,
              
              -- Total marked students
              (SELECT COUNT(*)
                 FROM SubmitterList
                WHERE EXISTS(
                      SELECT CourseAssignmentMark.camUid
                        FROM CourseAssignmentMark
                       WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
                         AND CourseAssignmentMark.camSubmitterUid = SubmitterList.submitterId) 
                ) AS markedStudents,
              
              -- Total who have submitted  
              (SELECT COUNT(*)
                 FROM SubmitterList
                WHERE EXISTS(
                      SELECT CourseAssignmentSubmission.casUid
                        FROM CourseAssignmentSubmission
                       WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                         AND CourseAssignmentSubmission.casSubmitterUid = SubmitterList.submitterId)
                ) AS submittedStudents,
              
              (SELECT (ClazzAssignment.caGroupUid != 0)
                 FROM ClazzAssignment
                WHERE ClazzAssignment.caUid = :assignmentUid) AS isGroupAssignment
    """)
    @QueryLiveTables(
        arrayOf(
            "SystemPermission", "CoursePermission", "ClazzAssignment",
            "ClazzEnrolment", "PeerReviewerAllocation", "Person", "CourseGroupMember",
            "CourseAssignmentSubmission", "CourseAssignmentMark", "Comments",
            "PersonPicture"
        )
    )
    /**
     * Get a summary of the numbers that have submitted/been marked for a given assignment.
     */
    abstract fun getProgressSummaryForAssignment(
        assignmentUid: Long,
        clazzUid: Long,
        accountPersonUid: Long,
        group: String,
    ): Flow<AssignmentProgressSummary?>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "getAssignmentSubmitterSummaryListForAssignment"
            ),
            //Get permission entities for this assignment / user
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2",
                functionDao = CoursePermissionDao::class,
            ),
            HttpServerFunctionCall(
                functionName = "findAllByPersonUidEntities",
                functionDao = SystemPermissionDao::class,
            ),
            //Get the assignment entity itself
            HttpServerFunctionCall(
                functionName = "findByUidAsync",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "uid",
                        argType = HttpServerFunctionParam.ArgType.MAP_OTHER_PARAM,
                        fromName = "assignmentUid"
                    )
                )

            ),
            //Get all ClazzEnrolment and Person entities for this assignment
            HttpServerFunctionCall(
                functionName = "findEnrolmentsAndPersonByClazzUidWithPermissionCheck",
                functionDao = ClazzEnrolmentDao::class,
            ),
            //Get CourseGroupMember entities for this assignment
            HttpServerFunctionCall(
                functionName = "getCourseGroupMembersByAssignmentUid"
            ),
            //Submissinos
            HttpServerFunctionCall(
                functionName = "getAssignmentSubmissionsByAssignmentUid"
            ),
            //and marks
            HttpServerFunctionCall(
                functionName = "getAssignmentMarksByAssignmentUid"
            ),
            //PeerReviewAllocations
            HttpServerFunctionCall(
                functionName = "getPeerReviewerAllocationsByAssignmentUid"
            )
        )
    )
    @Query("""
        WITH $HAS_LEARNINGRECORD_AND_MEMBER_VIEW_PERMISSION_CTE_SQL,
             $ASSIGNMENT_CLAZZ_UID_CTE_SQL,
             $SUBMITTER_LIST_CTE2_SQL
        
        SELECT SubmitterList.name AS name,
               SubmitterList.submitterId AS submitterUid,
               Comments.commentsText AS latestPrivateComment,
               CASE 
               WHEN SubmitterList.submitterId >= $MIN_SUBMITTER_UID_FOR_PERSON THEN
                    (SELECT PersonPicture.personPictureThumbnailUri
                       FROM PersonPicture
                      WHERE PersonPicture.personPictureUid =  SubmitterList.submitterId)
               ELSE NULL
               END AS pictureUri,       
               -- Determine submission status - marked, submitted, or not yet submitted
               CASE 
               WHEN CourseAssignmentMark.camUid IS NOT NULL THEN ${CourseAssignmentSubmission.MARKED}
               WHEN CourseAssignmentSubmission.casUid IS NOT NULL THEN ${CourseAssignmentSubmission.SUBMITTED}
               ELSE ${CourseAssignmentSubmission.NOT_SUBMITTED} 
               END AS fileSubmissionStatus
               
          FROM SubmitterList
               LEFT JOIN Comments 
                         ON Comments.commentsUid = 
                            (SELECT Comments.commentsUid 
                               FROM Comments
                              WHERE Comments.commentsEntityUid = :assignmentUid
                                AND Comments.commentsForSubmitterUid = SubmitterList.submitterId
                                AND NOT Comments.commentsDeleted
                           ORDER BY Comments.commentsDateTimeAdded DESC     
                              LIMIT 1) 
               LEFT JOIN CourseAssignmentMark
                         ON CourseAssignmentMark.camUid = 
                            (SELECT camUid
                               FROM CourseAssignmentMark
                              WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
                                AND CourseAssignmentMark.camSubmitterUid = SubmitterList.submitterId
                              LIMIT 1)
               LEFT JOIN CourseAssignmentSubmission
                         ON CourseAssignmentSubmission.casUid = 
                            (SELECT casUid
                               FROM CourseAssignmentSubmission
                              WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                                AND CourseAssignmentSubmission.casSubmitterUid = SubmitterList.submitterId 
                              LIMIT 1)
         WHERE (:searchText = '%' OR SubmitterList.name LIKE :searchText)
      ORDER BY CASE(:sortOption)
               WHEN $SORT_NAME_ASC THEN SubmitterList.name
               ELSE '' END ASC,
               CASE(:sortOption)
               WHEN $SORT_NAME_DESC THEN SubmitterList.name
               ELSE '' END DESC
    """)
    @QueryLiveTables(
        arrayOf(
            "SystemPermission", "CoursePermission", "ClazzAssignment",
            "ClazzEnrolment", "PeerReviewerAllocation", "Person", "CourseGroupMember",
            "CourseAssignmentSubmission", "CourseAssignmentMark", "Comments",
            "PersonPicture"
        )
    )
    /**
     * Used by the ClazzAssignmentDetailSubmissionsListTab - gets a list of the name (e.g. the
     * person name when submissions are by individual students, "group (groupnum)" when submissions
     * are by group.
     *
     * For each submitter, get the status (not submitted, submitted, marked) and the most recent
     * private comment (if any).
     */
    abstract fun getAssignmentSubmitterSummaryListForAssignment(
        assignmentUid: Long,
        clazzUid: Long,
        accountPersonUid: Long,
        group: String,
        searchText: String,
        sortOption: Int,
    ): PagingSource<Int, AssignmentSubmitterSummary>

    @Query("""
        SELECT CourseGroupMember.*
          FROM CourseGroupMember
         WHERE CourseGroupMember.cgmSetUid = 
               (SELECT ClazzAssignment.caGroupUid
                  FROM ClazzAssignment
                 WHERE ClazzAssignment.caUid = :assignmentUid) 
    """)
    abstract suspend fun getCourseGroupMembersByAssignmentUid(
        assignmentUid: Long,
    ): List<CourseGroupMember>

    /**
     * Get all submission entities for the given assignment
     */
    @Query("""
        WITH $HAS_LEARNINGRECORD_AND_MEMBER_VIEW_PERMISSION_CTE_SQL,
             $ASSIGNMENT_CLAZZ_UID_CTE_SQL,
             $SUBMITTER_LIST_CTE2_SQL
      SELECT CourseAssignmentSubmission.*
        FROM CourseAssignmentSubmission 
       WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
         AND CourseAssignmentSubmission.casClazzUid = :clazzUid
         AND CourseAssignmentSubmission.casSubmitterUid IN 
             (SELECT SubmitterList.submitterId
                FROM SubmitterList) 
    """
    )
    abstract suspend fun getAssignmentSubmissionsByAssignmentUid(
        assignmentUid: Long,
        clazzUid: Long,
        accountPersonUid: Long,
        group: String,
    ): List<CourseAssignmentSubmission>

    /**
     * Get all marks for the given course assignment that the person (as per personAccountUid) can
     * access
     */
    @Query("""
        WITH $HAS_LEARNINGRECORD_AND_MEMBER_VIEW_PERMISSION_CTE_SQL,
             $ASSIGNMENT_CLAZZ_UID_CTE_SQL,
             $SUBMITTER_LIST_CTE2_SQL
      SELECT CourseAssignmentMark.*
        FROM CourseAssignmentMark 
       WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
         AND CourseAssignmentMark.camClazzUid = :clazzUid
         AND CourseAssignmentMark.camSubmitterUid IN 
             (SELECT SubmitterList.submitterId
                FROM SubmitterList)
    """)
    abstract suspend fun getAssignmentMarksByAssignmentUid(
        assignmentUid: Long,
        clazzUid: Long,
        accountPersonUid: Long,
        group: String
    ): List<CourseAssignmentMark>

    /**
     * Used by getAssignmentSubmitterSummaryListForAssignment - only needs PeerReviewerAllocation(s)
     * for those who do not have the overall Learning Record Select permission e.g. so the query
     * can decide which submitters to show.
     *
     * Gets the PeerReviewerAllocations indicating the who the active user has been assigned to mark.
     */
    @Query("""
          WITH $HAS_LEARNINGRECORD_AND_MEMBER_VIEW_PERMISSION_CTE_SQL
        SELECT PeerReviewerAllocation.*
          FROM PeerReviewerAllocation
         WHERE $SELECT_ASSIGNMENT_IS_PEERMARKED_SQL
          AND NOT 
              (SELECT hasPermission 
                 FROM HasLearningRecordSelectPermission) 
           AND PeerReviewerAllocation.praAssignmentUid = :assignmentUid
           AND (   PeerReviewerAllocation.praMarkerSubmitterUid = :accountPersonUid
                OR PeerReviewerAllocation.praMarkerSubmitterUid IN 
                   (SELECT CourseGroupMember.cgmGroupNumber
                      FROM CourseGroupMember
                     WHERE CourseGroupMember.cgmSetUid = 
                           (SELECT ClazzAssignment.caGroupUid
                              FROM ClazzAssignment
                             WHERE ClazzAssignment.caUid = :assignmentUid)))
    """)
    abstract suspend fun getPeerReviewerAllocationsByAssignmentUid(
        assignmentUid: Long,
        clazzUid: Long,
        accountPersonUid: Long,
    ): List<PeerReviewerAllocation>

    /**
     * Get a list of the applicable PeerReviewerAllocations for the given accountPersonUid where
     * the AccountPersonUid is the marker (either directly as an individual or as a group member)
     */
    @Query("""
        SELECT PeerReviewerAllocation.*
          FROM PeerReviewerAllocation
         WHERE PeerReviewerAllocation.praAssignmentUid = :assignmentUid
           AND (PeerReviewerAllocation.praToMarkerSubmitterUid = :accountPersonUid
                OR
                PeerReviewerAllocation.praToMarkerSubmitterUid IN 
                (SELECT CourseGroupMember.cgmSetUid 
                   FROM CourseGroupMember
                  WHERE CourseGroupMember.cgmSetUid = 
                        (SELECT ClazzAssignment.caGroupUid
                           FROM ClazzAssignment
                          WHERE ClazzAssignment.caUid = :assignmentUid)
                    AND CourseGroupMember.cgmPersonUid = :accountPersonUid))                 
    """)
    abstract suspend fun getPeerReviewAllocationsForPerson(
        assignmentUid: Long,
        accountPersonUid: Long,
    ): List<PeerReviewerAllocation>

    /**
     * Used by UpdatePeerReviewAllocationUseCase - which needs to run even before the assignment
     * is saved to the database. Finds a list of all the expected submitter ids - list of enrolled
     * student personuids if the assignment is for individual submission, list of group numbers if
     * assignment is by groups.
     */
    @Query("""
         -- Submitter UIDs for individual assignment the list of personuids enrolled in the course
         SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid AS submitterUid
           FROM ClazzEnrolment
          WHERE (:groupSetUid = 0)
            AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
            AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
            AND :time BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft
          
         UNION
         
        SELECT DISTINCT CourseGroupMember.cgmGroupNumber AS submitterUid
          FROM CourseGroupMember
         WHERE :groupSetUid != 0
           AND CourseGroupMember.cgmSetUid = :groupSetUid         
    """)
    abstract suspend fun getSubmitterUidsByClazzOrGroupSetUid(
        clazzUid: Long,
        groupSetUid: Long,
        time: Long
    ): List<Long>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "getAllClazzEnrolledAtTimeAsync",
                functionDao = ClazzEnrolmentDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "roleFilter",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "${ClazzEnrolment.ROLE_STUDENT}",
                    ),
                    HttpServerFunctionParam(
                        name = "personUidFilter",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "0"
                    )
                )
            ),
            HttpServerFunctionCall(
                functionName = "findByGroupSetUidAsync",
                functionDao = CourseGroupMemberDao::class,
            )
        )
    )
    /**
     * Query to get a list of submitter uids and names. This query is used by assignment edit /
     * peer reviewer allocation edit, so it has to work before the asignment is saved to the
     * database (e.g. does not rely on assignment uid).
     */
    @Query("""
        WITH SubmitterUids(submitterUid) AS (
            SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid AS submitterUid
               FROM ClazzEnrolment
              WHERE (:groupSetUid = 0)
                AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
                AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                AND :date BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft
              
             UNION
             
            SELECT DISTINCT CourseGroupMember.cgmGroupNumber AS submitterUid
              FROM CourseGroupMember
             WHERE :groupSetUid != 0
               AND CourseGroupMember.cgmSetUid = :groupSetUid    
        )
        
        SELECT SubmitterUids.submitterUid AS submitterUid,
               CASE :groupSetUid
               WHEN 0 THEN
                      (SELECT Person.firstNames || ' ' || Person.lastName
                         FROM Person
                        WHERE Person.personUid = SubmitterUids.submitterUid)
               ELSE (:groupStr || ' ' || SubmitterUids.submitterUid)   
               END AS name
          FROM SubmitterUids                  
    """)
    abstract suspend fun getSubmitterUidsAndNameByClazzOrGroupSetUid(
        clazzUid: Long,
        groupSetUid: Long,
        date: Long,
        groupStr: String,
    ): List<AssignmentSubmitterUidAndName>



    @Query("""
         $SUBMITTER_LIST_WITHOUT_ASSIGNMENT_CTE
        
         SELECT COUNT(*) 
          FROM SubmitterList
    """)
    abstract suspend fun getSubmitterCountFromAssignment(
        groupUid: Long,
        clazzUid: Long,
        group: String
    ): Int


    //Needs enrolment for person
    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            //Get the assignment itself
            HttpServerFunctionCall(
                functionName = "findByUidAsync",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "uid",
                        argType = HttpServerFunctionParam.ArgType.MAP_OTHER_PARAM,
                        fromName = "assignmentUid"
                    )
                )
            ),
            //Get Person and Enrolment entities for the accountpersonuid
            HttpServerFunctionCall(
                functionName = "findEnrolmentsAndPersonByClazzUidWithPermissionCheck",
                functionDao = ClazzEnrolmentDao::class,
            ),
            //Get the CourseGroupMember if any for the accountpersonuid
            HttpServerFunctionCall(
                functionName = "findCourseGroupMembersByPersonUidAndAssignmentUid",
            )
        )
    )
    //Note: clazzUid is used in http replicate query calls, it is added within the query because
    //Room does not allow unused parameters.
    @Query("""
        WITH ClazzUidDummy(clazzUid) AS
             (SELECT :clazzUid)
             
        $SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL
    """)
    abstract suspend fun getSubmitterUid(
        assignmentUid: Long,
        clazzUid: Long,
        accountPersonUid: Long
    ): Long

    @Update
    abstract suspend fun updateAsync(clazzAssignment: ClazzAssignment)

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT * 
          FROM ClazzAssignment 
         WHERE caUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): ClazzAssignment?


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT * 
          FROM ClazzAssignment 
         WHERE caUid = :uid
           AND caClazzUid = :clazzUid
    """)
    abstract fun findByUidAndClazzUidAsFlow(uid: Long, clazzUid: Long): Flow<ClazzAssignment?>

    @Query("""
        SELECT ClazzAssignment.* 
          FROM ClazzAssignment 
         WHERE ClazzAssignment.caUid = :assignmentUid
           AND ClazzAssignment.caClazzUid = :clazzUid
    """)
    abstract suspend fun findByUidAndClazzUidAsync(assignmentUid: Long, clazzUid: Long): ClazzAssignment?

    @Query("""
          SELECT COALESCE((
           SELECT caGroupUid
           FROM ClazzAssignment
          WHERE caUid = :uid),-1)
    """)
    abstract suspend fun getGroupUidFromAssignment(uid: Long): Long

    @Query("""
          SELECT COALESCE((
           SELECT caMarkingType
           FROM ClazzAssignment
          WHERE caUid = :uid),-1)
    """)
    abstract suspend fun getMarkingTypeFromAssignment(uid: Long): Int

    @HttpAccessible(

    )
    @Query("""
        SELECT * 
          FROM ClazzAssignment
               LEFT JOIN CourseBlock
               ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
               AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
         WHERE caUid = :uid
    """)
    abstract suspend fun findByUidWithBlockAsync(uid: Long): ClazzAssignmentAndBlock?

    @Query("""
        SELECT * 
          FROM ClazzAssignment LIMIT 1
    """)
    abstract fun findClazzAssignment(): ClazzAssignment?

    @Query("""SELECT * 
                      FROM ClazzAssignment 
                     WHERE caUid = :uid""")
    abstract fun findByUidLive(uid: Long): Flow<ClazzAssignment?>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entities: List<ClazzAssignment>)

    @Query("""
        UPDATE ClazzAssignment
           SET caActive = :active,
               caLct = :changeTime
         WHERE caUid IN (:uidList)   
    """)
    abstract suspend fun updateActiveByList(
        uidList: List<Long>,
        active: Boolean,
        changeTime: Long
    )

    /**
     * Get the CourseBlock for the assignment and the submitter id (see
     * CourseAssignmentSubmission.casSubmitterUid ) for the accountPersonUid.
     */
    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findAssignmentCourseBlockAndSubmitterUidAsFlow"),
            //Get the ClazzEnrolment objects to correctly determine if active person is student
            HttpServerFunctionCall("findEnrolmentsByPersonUidAndAssignmentUid"),
            //Get the CourseGroupMember entities that apply for this assignment and personUid if any
            HttpServerFunctionCall("findCourseGroupMembersByPersonUidAndAssignmentUid"),
            //PeerReviewerAllocations if required
            HttpServerFunctionCall("findPeerReviewerAllocationsByPersonUidAndAssignmentUid"),
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2",
                functionDao = CoursePermissionDao::class,
            ),
            HttpServerFunctionCall(
                functionName = "findAllByPersonUidEntities",
                functionDao = SystemPermissionDao::class,
            )
        )
    )
    @Query("""
        WITH PersonIsStudent(isStudent)
             AS (SELECT EXISTS(
                        SELECT ClazzEnrolment.clazzEnrolmentPersonUid
                           FROM ClazzEnrolment
                          WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                            AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
                            AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}))
                        
        SELECT ClazzAssignment.*,
               CourseBlock.*,
               CourseBlockPicture.*,
               CourseGroupSet.*,
               ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL) AS submitterUid,
               
               (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} ${PermissionFlags.COURSE_MODERATE}
                ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} ${PermissionFlags.COURSE_MODERATE}
                ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3}) AS hasModeratePermission
                   
          FROM ClazzAssignment
               JOIN CourseBlock
                    ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
               LEFT JOIN CourseBlockPicture
                    ON CourseBlockPicture.cbpUid = CourseBlock.cbUid
               LEFT JOIN CourseGroupSet
                    ON CourseGroupSet.cgsUid = ClazzAssignment.caGroupUid
         WHERE ClazzAssignment.caUid = :assignmentUid
           AND ClazzAssignment.caClazzUid = :clazzUid
           AND (
                ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} ${PermissionFlags.COURSE_VIEW}
                ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} ${PermissionFlags.COURSE_VIEW}
                ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})
    """)
    @QueryLiveTables(arrayOf("Person", "ClazzAssignment", "CourseBlock", "CourseGroupMember",
        "ClazzEnrolment", "CoursePermission", "SystemPermission", "CourseBlockPicture"))
    abstract fun findAssignmentCourseBlockAndSubmitterUidAsFlow(
        assignmentUid: Long,
        clazzUid: Long,
        accountPersonUid: Long,
    ): Flow<ClazzAssignmentCourseBlockAndSubmitterUid?>


    /**
     * Get the ClazzEnrolment entities applicable for the given assignment uid and person uid
     */
    @Query("""
        SELECT ClazzEnrolment.*
          FROM ClazzEnrolment
         WHERE ClazzEnrolment.clazzEnrolmentClazzUid = 
               (SELECT ClazzAssignment.caClazzUid
                  FROM ClazzAssignment
                 WHERE ClazzAssignment.caUid = :assignmentUid)
           AND ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid 
    """)
    abstract suspend fun findEnrolmentsByPersonUidAndAssignmentUid(
        assignmentUid: Long,
        accountPersonUid: Long,
    ): List<ClazzEnrolment>

    @Query("""
        SELECT CourseGroupMember.*
          FROM CourseGroupMember
         WHERE CourseGroupMember.cgmSetUid = 
               (SELECT ClazzAssignment.caGroupUid 
                  FROM ClazzAssignment
                 WHERE ClazzAssignment.caUid = :assignmentUid)
           AND CourseGroupMember.cgmPersonUid = :accountPersonUid
    """)
    abstract suspend fun findCourseGroupMembersByPersonUidAndAssignmentUid(
        assignmentUid: Long,
        accountPersonUid: Long,
    ): List<CourseGroupMember>

    /**
     * Get the PeerReviewerAllocations that are required for the given assignment and the given
     * active user as per accountPersonUid: this is effectively all PeerReviewerAllocations that
     * either assign what the active user should be marking, or who will mark them
     */
    @Query("""
        SELECT PeerReviewerAllocation.*
          FROM PeerReviewerAllocation
         WHERE PeerReviewerAllocation.praAssignmentUid = :assignmentUid
           AND (
                    PeerReviewerAllocation.praMarkerSubmitterUid = :accountPersonUid
                 OR PeerReviewerAllocation.praToMarkerSubmitterUid = :accountPersonUid
                 OR PeerReviewerAllocation.praMarkerSubmitterUid IN
                    (SELECT CourseGroupMember.cgmGroupNumber
                       FROM CourseGroupMember
                      WHERE CourseGroupMember.cgmSetUid = 
                            (SELECT ClazzAssignment.caGroupUid
                               FROM ClazzAssignment
                              WHERE ClazzAssignment.caUid = :assignmentUid)
                        AND CourseGroupMember.cgmPersonUid = :accountPersonUid)
                 OR PeerReviewerAllocation.praToMarkerSubmitterUid IN
                    (SELECT CourseGroupMember.cgmGroupNumber
                       FROM CourseGroupMember
                      WHERE CourseGroupMember.cgmSetUid = 
                            (SELECT ClazzAssignment.caGroupUid
                               FROM ClazzAssignment
                              WHERE ClazzAssignment.caUid = :assignmentUid)
                                AND CourseGroupMember.cgmPersonUid = :accountPersonUid))
    """)
    abstract suspend fun findPeerReviewerAllocationsByPersonUidAndAssignmentUid(
        assignmentUid: Long,
        accountPersonUid: Long
    ): List<PeerReviewerAllocation>

    @Query("""
        WITH CourseBlockDeadlines(deadline, gracePeriod) AS
             (SELECT CourseBlock.cbDeadlineDate AS deadline,
                     CourseBlock.cbGracePeriodDate AS gracePeriod
                FROM CourseBlock
               WHERE CourseBlock.cbEntityUid = :assignmentUid
                 AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
               LIMIT 1)
        SELECT CASE
               WHEN (SELECT gracePeriod 
                       FROM CourseBlockDeadlines)
                    BETWEEN 1 AND $MAX_VALID_DATE THEN (SELECT gracePeriod FROM CourseBlockDeadlines)
               ELSE (SELECT deadline FROM CourseBlockDeadlines)
               END AS latestSubmissionTimeAllowed
    """)
    abstract suspend fun getLatestSubmissionTimeAllowed(assignmentUid: Long): Long
}