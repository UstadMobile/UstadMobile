package com.ustadmobile.core.db.dao


import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.CourseBlockDaoCommon.SUBMITTER_LIST_IN_CLAZZ_CTE
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.door.annotation.QueryLiveTables
import app.cash.paging.PagingSource
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.composites.CourseBlockUidAndClazzUid
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow
import kotlin.js.JsName

@Repository
@DoorDao
expect abstract class CourseBlockDao : BaseDao<CourseBlock>, OneToManyJoinDao<CourseBlock> {

    @JsName("findByUid")
    @Query("SELECT * FROM CourseBlock WHERE cbUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): CourseBlock?

    @Update
    abstract suspend fun updateAsync(entity: CourseBlock): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceListAsync(list: List<CourseBlock>)


    @Query("""
        SELECT CourseBlock.*, assignment.*, courseDiscussion.*, entry.*, Language.*,
               (SELECT CourseGroupSet.cgsName
                  FROM CourseGroupSet
                 WHERE CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                   AND assignment.caGroupUid != 0
                   AND CourseGroupSet.cgsUid = assignment.caGroupUid) AS assignmentCourseGroupSetName
          FROM CourseBlock 
               LEFT JOIN ClazzAssignment as assignment
                         ON assignment.caUid = CourseBlock.cbEntityUid
                            AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
               LEFT JOIN CourseDiscussion as courseDiscussion
                         ON CourseDiscussion.courseDiscussionUid = CourseBlock.cbEntityUid
                            AND CourseBlock.cbType = ${CourseBlock.BLOCK_DISCUSSION_TYPE}
               LEFT JOIN ContentEntry as entry
                         ON entry.contentEntryUid = CourseBlock.cbEntityUid
                            AND CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
               LEFT JOIN Language
                         ON Language.langUid = entry.primaryLanguageUid
                            AND CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
               
         WHERE CourseBlock.cbClazzUid = :clazzUid
           AND CourseBlock.cbActive
      ORDER BY CourseBlock.cbIndex
          """)
    abstract suspend fun findAllCourseBlockByClazzUidAsync(clazzUid: Long): List<CourseBlockWithEntityDb>

    @Query("""
         WITH CtePermissionCheck (hasPermission) 
            AS (SELECT EXISTS( 
               SELECT PrsGrpMbr.groupMemberPersonUid
                  FROM Clazz
                       ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                          ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}
                          ${Clazz.JOIN_FROM_SCOPEDGRANT_TO_PERSONGROUPMEMBER}
                 WHERE Clazz.clazzUid = :clazzUid
                   AND PrsGrpMbr.groupMemberPersonUid = :personUid)), 
                   
        $SUBMITTER_LIST_IN_CLAZZ_CTE, 
        
        ScoreByMarker (score, penalty, assignmentUid, camSubmitterUid) AS (
                 SELECT camMark, camPenalty, camAssignmentUid, camSubmitterUid
                   FROM courseAssignmentMark
                        JOIN ClazzAssignment
                        ON ClazzAssignment.caUid = courseAssignmentMark.camAssignmentUid        
                  WHERE camLct = (SELECT MAX(mark.camLct) 
                                    FROM CourseAssignmentMark As mark
                                    WHERE mark.camAssignmentUid = ClazzAssignment.caUid
                                     AND (caMarkingType = 1
                                       OR mark.camMarkerSubmitterUid = courseAssignmentMark.camMarkerSubmitterUid))
                                                                                   
                )     
                   

        SELECT CourseBlock.*, ClazzAssignment.*, ContentEntry.*, CourseDiscussion.*, ContentEntryParentChildJoin.*, 
               Container.*, CourseAssignmentMark.*, (CourseBlock.cbUid NOT IN (:collapseList)) AS expanded,
               
               COALESCE(StatementEntity.resultScoreMax,0) AS resultMax, 
                COALESCE(StatementEntity.resultScoreRaw,0) AS resultScore, 
                COALESCE(StatementEntity.resultScoreScaled,0) AS resultScaled, 
                COALESCE(StatementEntity.extensionProgress,0) AS progress, 
                COALESCE(StatementEntity.resultCompletion,'FALSE') AS contentComplete,
                COALESCE(StatementEntity.resultSuccess, 0) AS success,
                
                COALESCE((CASE WHEN StatementEntity.resultCompletion 
                THEN 1 ELSE 0 END),0) AS totalCompletedContent,
                
                0 AS assignmentContentWeight,
                1 as totalContent, 
                0 as penalty,
                
                (SELECT hasPermission FROM CtePermissionCheck) AS hasMetricsPermission,
                
             
                 (SELECT COUNT(*) 
                    FROM SubmitterList 
                   WHERE SubmitterList.assignmentUid = ClazzAssignment.caUid) 
                        AS totalStudents, 
 
               0 AS notSubmittedStudents,
               
              (SELECT COUNT(DISTINCT CourseAssignmentSubmission.casSubmitterUid) 
                             FROM CourseAssignmentSubmission
                                   LEFT JOIN CourseAssignmentMark
                                   ON CourseAssignmentSubmission.casSubmitterUid = CourseAssignmentMark.camSubmitterUid
                                   AND CourseAssignmentMark.camAssignmentUid = CourseAssignmentSubmission.casAssignmentUid
                            WHERE CourseAssignmentMark.camUid IS NULL
                              AND CourseAssignmentSubmission.casAssignmentUid = ClazzAssignment.caUid
                              AND CourseAssignmentSubmission.casSubmitterUid IN 
                                                    (SELECT submitterId 
                                                      FROM SubmitterList
                                                     WHERE SubmitterList.assignmentUid = ClazzAssignment.caUid)
                    ) AS submittedStudents,         
               
                (SELECT COUNT(DISTINCT CourseAssignmentMark.camSubmitterUid) 
                           FROM CourseAssignmentMark
                            
                             JOIN CourseAssignmentSubmission
                             ON CourseAssignmentSubmission.casSubmitterUid = CourseAssignmentMark.camSubmitterUid
                             AND CourseAssignmentSubmission.casAssignmentUid = CourseAssignmentMark.camAssignmentUid
                             
                          WHERE CourseAssignmentMark.camAssignmentUid = ClazzAssignment.caUid 
                            AND CourseAssignmentMark.camSubmitterUid IN (SELECT submitterId 
                                                                            FROM SubmitterList
                                                                           WHERE SubmitterList.assignmentUid = ClazzAssignment.caUid))
                   AS markedStudents,
                   
                   (ClazzAssignment.caGroupUid != 0) AS isGroupAssignment,
                   
                   COALESCE((CASE WHEN CourseAssignmentMark.camUid IS NOT NULL 
                          THEN ${CourseAssignmentSubmission.MARKED} 
                          WHEN CourseAssignmentSubmission.casUid IS NOT NULL 
                          THEN ${CourseAssignmentSubmission.SUBMITTED} 
                          ELSE ${CourseAssignmentSubmission.NOT_SUBMITTED} END), 
                               ${CourseAssignmentSubmission.NOT_SUBMITTED}) AS fileSubmissionStatus,
                               
                  (SELECT AVG(score) 
                     FROM ScoreByMarker 
                    WHERE assignmentUid = ClazzAssignment.caUid 
                      AND camSubmitterUid = (SELECT (CASE WHEN ref.caGroupUid = 0 
                                                          THEN :personUid 
                                                          WHEN CourseGroupMember.cgmUid IS NULL 
                                                          THEN 0 
                                                          ELSE CourseGroupMember.cgmGroupNumber 
                                                           END) as submitterUid
                                               FROM ClazzAssignment AS ref
                                                     LEFT JOIN CourseGroupMember
                                                     ON cgmSetUid = ClazzAssignment.caGroupUid
                                                     AND cgmPersonUid = :personUid
                                               WHERE ref.caUid = ClazzAssignment.caUid)) as averageScore,
                                                            
                 (SELECT AVG(penalty) 
                     FROM ScoreByMarker 
                    WHERE assignmentUid = ClazzAssignment.caUid 
                      AND camSubmitterUid = (SELECT (CASE WHEN ref.caGroupUid = 0 
                                                          THEN :personUid 
                                                          WHEN CourseGroupMember.cgmUid IS NULL 
                                                          THEN 0 
                                                          ELSE CourseGroupMember.cgmGroupNumber 
                                                           END) as submitterUid
                                               FROM ClazzAssignment AS ref
                                                     LEFT JOIN CourseGroupMember
                                                     ON cgmSetUid = ClazzAssignment.caGroupUid
                                                     AND cgmPersonUid = :personUid
                                               WHERE ref.caUid = ClazzAssignment.caUid)) as averagePenalty                                             
                                      
                
          FROM CourseBlock 
          
               LEFT JOIN CourseBlock AS parentBlock
               ON CourseBlock.cbModuleParentBlockUid = parentBlock.cbUid
               AND CourseBlock.cbTYpe != ${CourseBlock.BLOCK_MODULE_TYPE}
          
               LEFT JOIN ClazzAssignment
               ON ClazzAssignment.caUid = CourseBlock.cbEntityUid
               AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
               
               LEFT JOIN ContentEntry
               ON ContentEntry.contentEntryUid = CourseBlock.cbEntityUid
               AND NOT ceInactive
               AND CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
               
               LEFT JOIN CourseDiscussion 
                      ON CourseDiscussion.courseDiscussionUid = CourseBlock.cbEntityUid
                     AND CourseBlock.cbType = ${CourseBlock.BLOCK_DISCUSSION_TYPE}
               
               LEFT JOIN ContentEntryParentChildJoin 
               ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid
               
               LEFT JOIN Container 
                    ON Container.containerUid = 
                        (SELECT containerUid 
                           FROM Container 
                          WHERE containerContentEntryUid = ContentEntry.contentEntryUid 
                       ORDER BY cntLastModified DESC LIMIT 1)
                       
              LEFT JOIN StatementEntity
				     ON StatementEntity.statementUid = 
                                (SELECT statementUid 
							       FROM StatementEntity 
                                  WHERE statementContentEntryUid = ContentEntry.contentEntryUid 
							        AND StatementEntity.statementPersonUid = :personUid
							        AND contentEntryRoot 
                               ORDER BY resultScoreScaled DESC, 
                                        extensionProgress DESC, 
                                        resultSuccess DESC 
                                  LIMIT 1) 
                                  
               LEFT JOIN CourseAssignmentSubmission
                ON casUid = (SELECT casUid 
                                     FROM CourseAssignmentSubmission
                                    WHERE casAssignmentUid = ClazzAssignment.caUid
                                      AND casSubmitterUid = (SELECT (CASE WHEN ref.caGroupUid = 0 
                                                                          THEN :personUid 
                                                                          WHEN CourseGroupMember.cgmUid IS NULL 
                                                                          THEN 0 
                                                                          ELSE CourseGroupMember.cgmGroupNumber 
                                                                          END) as submitterUid
                                                               FROM ClazzAssignment AS ref
                                                                    LEFT JOIN CourseGroupMember
                                                                     ON cgmSetUid = ClazzAssignment.caGroupUid
                                                                     AND cgmPersonUid = :personUid
                                                              WHERE ref.caUid = ClazzAssignment.caUid)
                                 ORDER BY casTimestamp DESC
                                    LIMIT 1)
                                          
               LEFT JOIN CourseAssignmentMark
                      ON camUid = (SELECT camUid 
                                     FROM CourseAssignmentMark
                                    WHERE camAssignmentUid = ClazzAssignment.caUid
                                      AND camSubmitterUid = (SELECT (CASE WHEN ref.caGroupUid = 0 
                                                                          THEN :personUid 
                                                                          WHEN CourseGroupMember.cgmUid IS NULL 
                                                                          THEN 0 
                                                                          ELSE CourseGroupMember.cgmGroupNumber 
                                                                          END) as submitterUid
                                                               FROM ClazzAssignment AS ref
                                                                    LEFT JOIN CourseGroupMember
                                                                     ON cgmSetUid = ClazzAssignment.caGroupUid
                                                                     AND cgmPersonUid = :personUid
                                                              WHERE ref.caUid = ClazzAssignment.caUid)
                                 ORDER BY camLct DESC
                                    LIMIT 1)       
         WHERE CourseBlock.cbClazzUid = :clazzUid
           AND CourseBlock.cbActive
           AND NOT CourseBlock.cbHidden
           AND :currentTime > CourseBlock.cbHideUntilDate
           AND :currentTime > COALESCE(parentBlock.cbHideUntilDate,0)
           AND CourseBlock.cbModuleParentBlockUid NOT IN (:collapseList)
      ORDER BY CourseBlock.cbIndex
    """)
    @QueryLiveTables(value = ["CourseBlock", "ClazzAssignment", "CourseDiscussion",
        "ContentEntry", "CourseAssignmentMark","StatementEntity",
        "Container","ContentEntryParentChildJoin","PersonGroupMember",
        "Clazz","ScopedGrant","ClazzEnrolment","CourseAssignmentSubmission",
        "CourseGroupMember"])
    abstract fun findAllCourseBlockByClazzUidLive(
        clazzUid: Long,
        personUid: Long,
        collapseList: List<Long>,
        currentTime: Long
    ): PagingSource<Int, CourseBlockWithCompleteEntity>


    @HttpAccessible
    @Query("""
        SELECT CourseBlock.*,
               CourseBlock.cbUid NOT IN(:collapseList) AS expanded
          FROM CourseBlock
         WHERE CourseBlock.cbClazzUid = :clazzUid
           AND CourseBlock.cbModuleParentBlockUid NOT IN(:collapseList)
           AND CourseBlock.cbActive
      ORDER BY CourseBlock.cbIndex       
    """)
    abstract fun findAllCourseBlockByClazzUidAsPagingSource(
        clazzUid: Long,
        collapseList: List<Long>,
    ): PagingSource<Int, CourseBlockAndDisplayDetails>

    @Query("""
        UPDATE CourseBlock 
           SET cbActive = :active, 
               cbLct = :changeTime
         WHERE cbUid = :cbUid""")
    abstract suspend fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entities: List<CourseBlock>)

    @Query("""
        SELECT CourseBlock.cbTitle
          FROM CourseBlock 
         WHERE CourseBlock.cbEntityUid = :assignmentUid
           AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
    """)
    abstract fun getTitleByAssignmentUid(assignmentUid: Long) : Flow<String?>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT CourseBlock.*
          FROM CourseBlock
         WHERE CourseBlock.cbUid = :courseBlockUid 
    """)
    abstract fun findByUidAsFlow(courseBlockUid: Long): Flow<CourseBlock?>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findCourseBlockByDiscussionPostUid")
        )
    )
    @Query("""
        SELECT COALESCE(CourseBlock.cbUid, 0) AS courseBlockUid,
               COALESCE(CourseBlock.cbClazzUid, 0) AS clazzUid
          FROM CourseBlock
         WHERE CourseBlock.cbUid = 
               (SELECT DiscussionPost.discussionPostCourseBlockUid 
                  FROM DiscussionPost
                 WHERE DiscussionPost.discussionPostUid = :postUid)
         LIMIT 1
    """)
    abstract suspend fun findCourseBlockAndClazzUidByDiscussionPostUid(
        postUid: Long
    ): CourseBlockUidAndClazzUid?

    @Query("""
        SELECT CourseBlock.*
          FROM CourseBlock
         WHERE CourseBlock.cbUid = 
               (SELECT DiscussionPost.discussionPostCourseBlockUid 
                  FROM DiscussionPost
                 WHERE DiscussionPost.discussionPostUid = :postUid) 
    """)
    abstract suspend fun findCourseBlockByDiscussionPostUid(
        postUid: Long
    ): CourseBlock?

    @Query("""
        SELECT COALESCE(CourseBlock.cbClazzUid, 0) AS clazzUid
          FROM CourseBlock
         WHERE CourseBlock.cbUid = :courseBlockUid
    """)
    abstract suspend fun findClazzUidByCourseBlockUid(
        courseBlockUid: Long
    ): Long

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("""
        SELECT CourseBlock.*
          FROM CourseBlock
         WHERE CourseBlock.cbEntityUid = :assignmentUid
           AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
         LIMIT 1 
    """)
    abstract fun findCourseBlockByAssignmentUid(
        assignmentUid: Long
    ): Flow<CourseBlock?>
}