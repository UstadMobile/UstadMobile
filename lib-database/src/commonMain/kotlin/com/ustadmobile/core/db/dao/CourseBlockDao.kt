package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

@Repository
@Dao
abstract class CourseBlockDao : BaseDao<CourseBlock>, OneToManyJoinDao<CourseBlock> {

    @Query("""
    REPLACE INTO CourseBlockReplicate(cbPk, cbDestination)
      SELECT DISTINCT CourseBlock.cbUid AS cbPk,
             :newNodeId AS cbDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
               JOIN CourseBlock
                    ON CourseBlock.cbClazzUid = Clazz.clazzUid                
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND CourseBlock.cbLct != COALESCE(
             (SELECT cbVersionId
                FROM CourseBlockReplicate
               WHERE cbPk = CourseBlock.cbUid
                 AND cbDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(cbPk, cbDestination) DO UPDATE
             SET cbPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CourseBlock::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)



    @Query("""
         REPLACE INTO CourseBlockReplicate(cbPk, cbDestination)
  SELECT DISTINCT CourseBlock.cbUid AS cbPk,
         UserSession.usClientNodeId AS cbDestination
    FROM ChangeLog
         JOIN CourseBlock
             ON ChangeLog.chTableId = ${CourseBlock.TABLE_ID}
                AND ChangeLog.chEntityPk = CourseBlock.cbUid
             JOIN Clazz
                    ON  Clazz.clazzUid = CourseBlock.cbClazzUid
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_CLAZZ_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND CourseBlock.cbLct != COALESCE(
         (SELECT cbVersionId
            FROM CourseBlockReplicate
           WHERE cbPk = CourseBlock.cbUid
             AND cbDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(cbPk, cbDestination) DO UPDATE
     SET cbPending = true
  */               
    """)
    @ReplicationRunOnChange([CourseBlock::class])
    @ReplicationCheckPendingNotificationsFor([CourseBlock::class])
    abstract suspend fun replicateOnChange()

    @JsName("findByUid")
    @Query("SELECT * FROM CourseBlock WHERE cbUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): CourseBlock?

    @Update
    abstract suspend fun updateAsync(entity: CourseBlock): Int


    @Query("""
        SELECT * 
          FROM CourseBlock 
               LEFT JOIN ClazzAssignment as assignment
               ON assignment.caUid = CourseBlock.cbEntityUid
               AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
               LEFT JOIN ContentEntry as entry
               ON entry.contentEntryUid = CourseBlock.cbEntityUid
               AND CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
         WHERE cbClazzUid = :clazzUid
           AND cbActive
      ORDER BY cbIndex
          """)
    abstract suspend fun findAllCourseBlockByClazzUidAsync(clazzUid: Long): List<CourseBlockWithEntity>

    @Query("""
         WITH CtePermissionCheck (hasPermission) 
            AS (SELECT EXISTS( 
               SELECT PrsGrpMbr.groupMemberPersonUid
                  FROM Clazz
                       ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                          ${Role.PERMISSION_ASSIGNMENT_VIEWSTUDENTPROGRESS}
                          ${Clazz.JOIN_FROM_SCOPEDGRANT_TO_PERSONGROUPMEMBER}
                 WHERE Clazz.clazzUid = :clazzUid
                   AND PrsGrpMbr.groupMemberPersonUid = :personUid))

        SELECT CourseBlock.*, ClazzAssignment.*, ContentEntry.*, ContentEntryParentChildJoin.*, 
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
                        FROM ClazzEnrolment 
                        WHERE ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid 
                        AND ClazzEnrolment.clazzEnrolmentActive 
                        AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                        AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft) 
                        AS totalStudents, 
 
               0 AS notSubmittedStudents,
               
               (CASE WHEN (SELECT hasPermission 
                          FROM CtePermissionCheck)
                     THEN (SELECT COUNT(DISTINCT CourseAssignmentSubmission.casStudentUid)
                         FROM ClazzEnrolment
                              JOIN CourseAssignmentSubmission
                              ON ClazzEnrolment.clazzEnrolmentPersonUid = CourseAssignmentSubmission.casStudentUid
                              AND ClazzAssignment.caUid = CourseAssignmentSubmission.casAssignmentUid
                             
                              LEFT JOIN CourseAssignmentMark
                              ON ClazzEnrolment.clazzEnrolmentPersonUid = CourseAssignmentMark.camStudentUid
                              AND ClazzAssignment.caUid = CourseAssignmentMark.camAssignmentUid
                              
                        WHERE ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                          AND ClazzEnrolment.clazzEnrolmentActive
                          AND CourseAssignmentMark.camUid IS NULL
                          AND ClazzAssignment.caClazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
                          AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft) 
                ELSE 0 END) AS submittedStudents,         
               
                (CASE WHEN (SELECT hasPermission 
                           FROM CtePermissionCheck)
                   THEN (SELECT COUNT(DISTINCT(CourseAssignmentMark.camStudentUid)) 
                           FROM CourseAssignmentMark 
                                JOIN ClazzEnrolment
                                ON ClazzEnrolment.clazzEnrolmentPersonUid = CourseAssignmentMark.camStudentUid
                                
                          WHERE CourseAssignmentMark.camAssignmentUid = ClazzAssignment.caUid
                            AND ClazzEnrolment.clazzEnrolmentActive
                            AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                            AND ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
                            AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft)
                   ELSE 0 END) AS markedStudents,
                   
                   (CASE WHEN CourseAssignmentMark.camAssignmentUid IS NOT NULL 
                             THEN ${CourseAssignmentSubmission.MARKED}
                             ELSE ${CourseAssignmentSubmission.SUBMITTED} 
                             END) AS fileSubmissionStatus
                
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
               LEFT JOIN CourseAssignmentMark
                      ON camUid = (SELECT camUid 
                                     FROM CourseAssignmentMark
                                    WHERE camAssignmentUid = ClazzAssignment.caUid
                                      AND camStudentUid = :personUid
                                 ORDER BY camLct DESC
                                    LIMIT 1)
         WHERE CourseBlock.cbClazzUid = :clazzUid
           AND CourseBlock.cbActive
           AND NOT CourseBlock.cbHidden
           AND :currentTime > CourseBlock.cbHideUntilDate     
           AND CourseBlock.cbModuleParentBlockUid NOT IN (:collapseList)
      ORDER BY CourseBlock.cbIndex
    """)
    @QueryLiveTables(value = ["CourseBlock", "ClazzAssignment",
        "ContentEntry", "CourseAssignmentMark","StatementEntity",
        "Container","ContentEntryParentChildJoin","PersonGroupMember",
        "Clazz","ScopedGrant","ClazzEnrolment","CourseAssignmentSubmission"])
    abstract fun findAllCourseBlockByClazzUidLive(clazzUid: Long,
                                                  personUid: Long,
                                                  collapseList: List<Long>,
                                                  currentTime: Long):
            DoorDataSourceFactory<Int, CourseBlockWithCompleteEntity>


    @Query("""
        UPDATE CourseBlock 
           SET cbActive = :active, 
               cbLct = :changeTime
         WHERE cbUid = :cbUid""")
    abstract fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)

    override suspend fun deactivateByUids(uidList: List<Long>, changeTime: Long) {
        uidList.forEach {
            updateActiveByUid(it, false, changeTime)
        }
    }

}