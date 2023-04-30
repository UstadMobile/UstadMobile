package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.ASSIGNMENT_PERMISSION
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SUBMITTER_LIST_CTE
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SUBMITTER_LIST_WITHOUT_ASSIGNMENT_CTE
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow


@DoorDao
@Repository
expect abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment>, OneToManyJoinDao<ClazzAssignment> {

    @Query("""
     REPLACE INTO ClazzAssignmentReplicate(caPk, caDestination)
      SELECT DISTINCT ClazzAssignment.caUid AS caUid,
             :newNodeId AS caDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
             JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = Clazz.clazzUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND ClazzAssignment.caLct != COALESCE(
             (SELECT caVersionId
                FROM ClazzAssignmentReplicate
               WHERE caPk = ClazzAssignment.caUid
                 AND caDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(caPk, caDestination) DO UPDATE
             SET caPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ClazzAssignment::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ClazzAssignmentReplicate(caPk, caDestination)
  SELECT DISTINCT ClazzAssignment.caUid AS caUid,
         UserSession.usClientNodeId AS caDestination
    FROM ChangeLog
         JOIN ClazzAssignment
             ON ChangeLog.chTableId = ${ClazzAssignment.TABLE_ID}
                AND ChangeLog.chEntityPk = ClazzAssignment.caUid
         JOIN Clazz 
              ON Clazz.clazzUid = ClazzAssignment.caClazzUid 
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_ASSIGNMENT_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ClazzAssignment.caLct != COALESCE(
         (SELECT caVersionId
            FROM ClazzAssignmentReplicate
           WHERE caPk = ClazzAssignment.caUid
             AND caDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(caPk, caDestination) DO UPDATE
     SET caPending = true
  */               
 """)
    @ReplicationRunOnChange([ClazzAssignment::class])
    @ReplicationCheckPendingNotificationsFor([ClazzAssignment::class])
    abstract suspend fun replicateOnChange()

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
           $ASSIGNMENT_PERMISSION,
         
           $SUBMITTER_LIST_CTE
            
            SELECT (SELECT COUNT(*) FROM SubmitterList) AS totalStudents,
            
                    0 as notSubmittedStudents,
                    
                    (SELECT COUNT(DISTINCT CourseAssignmentSubmission.casSubmitterUid) 
                      FROM CourseAssignmentSubmission
                           LEFT JOIN CourseAssignmentMark
                           ON CourseAssignmentSubmission.casSubmitterUid = CourseAssignmentMark.camSubmitterUid
                           AND CourseAssignmentMark.camAssignmentUid = CourseAssignmentSubmission.casAssignmentUid
                     WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                       AND CourseAssignmentMark.camUid IS NULL
                       AND CourseAssignmentSubmission.casSubmitterUid IN (SELECT submitterId 
                                                                            FROM SubmitterList))
                      AS submittedStudents,
                     
                     
                     (SELECT COUNT(DISTINCT CourseAssignmentMark.camSubmitterUid) 
                        FROM CourseAssignmentMark
                            
                             JOIN CourseAssignmentSubmission
                             ON CourseAssignmentSubmission.casSubmitterUid = CourseAssignmentMark.camSubmitterUid
                             AND CourseAssignmentSubmission.casAssignmentUid = CourseAssignmentMark.camAssignmentUid
                             
                       WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
                         AND CourseAssignmentMark.camSubmitterUid IN (SELECT submitterId 
                                                                            FROM SubmitterList))
                         AS markedStudents, 
                         
                         'TRUE' AS hasMetricsPermission
                         
         FROM  ClazzAssignment
        WHERE caActive
          AND caClazzUid = :clazzUid 
          AND caUid = :assignmentUid                  
    """)
    abstract fun getProgressSummaryForAssignment(
        assignmentUid: Long,
        clazzUid: Long,
        group: String,
        submitterUid: Long,
        loggedInPersonUid: Long
    ) : LiveData<AssignmentProgressSummary?>


    @Query("""
         $ASSIGNMENT_PERMISSION,
         
         $SUBMITTER_LIST_CTE
        
         SELECT submitterId AS submitterUid,
                name, 
                
                 COALESCE((CASE WHEN CourseAssignmentMark.camUid IS NOT NULL 
                          THEN ${CourseAssignmentSubmission.MARKED} 
                          WHEN CourseAssignmentSubmission.casUid IS NOT NULL 
                          THEN ${CourseAssignmentSubmission.SUBMITTED} 
                          ELSE ${CourseAssignmentSubmission.NOT_SUBMITTED} END), 
                               ${CourseAssignmentSubmission.NOT_SUBMITTED}) AS fileSubmissionStatus,
                
                (CASE WHEN ClazzAssignment.caGroupUid = 0 
                 THEN 'TRUE' 
                 ELSE 'FALSE' END) AS isGroupAssignment,
                 
                 cm.commentsText AS latestPrivateComment 

           FROM SubmitterList
                JOIN ClazzAssignment
                ON ClazzAssignment.caUid = :assignmentUid
           
                LEFT JOIN CourseAssignmentMark
                ON CourseAssignmentMark.camUid = (SELECT camUid
                                                    FROM CourseAssignmentMark
                                                   WHERE camAssignmentUid = :assignmentUid
                                                     AND camSubmitterUid = SubmitterList.submitterId
                                                ORDER BY camLct DESC 
                                                   LIMIT 1)
                
                LEFT JOIN CourseAssignmentSubmission
                ON CourseAssignmentSubmission.casUid = (SELECT casUid
                                                          FROM CourseAssignmentSubmission
                                                         WHERE casAssignmentUid = :assignmentUid
                                                           AND casSubmitterUid = SubmitterList.submitterId
                                                      ORDER BY casTimestamp DESC 
                                                         LIMIT 1)
                LEFT JOIN Comments AS cm 
                    ON cm.commentsUid = (
                                 SELECT Comments.commentsUid 
                                   FROM Comments 
                                  WHERE Comments.commentsEntityType = ${ClazzAssignment.TABLE_ID}
                                    AND commentsEntityUid = :assignmentUid
                                    AND NOT commentsInActive
                                    AND NOT commentsPublic
                                    AND (CASE WHEN ClazzAssignment.caGroupUid = 0
                                              THEN commentsPersonUid = SubmitterList.submitterId
                                              ELSE commentSubmitterUid = SubmitterList.submitterId END)
                               ORDER BY commentsDateTimeAdded DESC LIMIT 1)                                                      
                                                                      
          WHERE name LIKE :searchText
       ORDER BY name 
    """)
    @QueryLiveTables(value = ["ClazzAssignment","CourseAssignmentMark","CourseAssignmentSubmission",
    "Comments", "ClazzEnrolment", "Person","PersonGroupMember","ScopedGrant","Clazz","PeerReviewerAllocation",
    "CourseGroupMember"])
    abstract fun getSubmitterListForAssignmentSummary(
        assignmentUid: Long,
        clazzUid: Long,
        group: String,
        searchText: String,
        submitterUid: Long,
        loggedInPersonUid: Long
    ): DataSourceFactory<Int, AssignmentSubmitterSummary>

    @Query("""
         $SUBMITTER_LIST_WITHOUT_ASSIGNMENT_CTE
        
         SELECT submitterId AS submitterUid,
                name, 
                0 as fileSubmissionStatus,
                (CASE WHEN :groupUid = 0 
                 THEN 'TRUE' 
                 ELSE 'FALSE' END) AS isGroupAssignment,
                 
                 '' as latestPrivateComment
                 
          FROM SubmitterList    
      ORDER BY name         
    """)
    abstract suspend fun getSubmitterListForAssignmentList(
        groupUid: Long,
        clazzUid: Long,
        group: String
    ): List<AssignmentSubmitterSummary>


    @Query("""
        $ASSIGNMENT_PERMISSION
        
        SELECT (CASE WHEN caMarkingType = ${ClazzAssignment.MARKED_BY_COURSE_LEADER}
                    THEN (SELECT hasPermission FROM AssignmentPermission)
                    ELSE PeerReviewerAllocation.praUid IS NOT NULL END)
          FROM ClazzAssignment
              
               LEFT JOIN PeerReviewerAllocation
               ON PeerReviewerAllocation.praToMarkerSubmitterUid = :selectedPersonUid
               AND PeerReviewerAllocation.praMarkerSubmitterUid = :submitterUid
               AND praActive
         WHERE caUid = :caUid 
    """)
    abstract suspend fun canMarkAssignment(
        caUid: Long,
        clazzUid: Long,
        loggedInPersonUid: Long,
        submitterUid: Long,
        selectedPersonUid: Long): Boolean



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


    @Query("""
        SELECT (CASE WHEN ClazzAssignment.caGroupUid = 0 
                     THEN :personUid 
                     WHEN CourseGroupMember.cgmUid IS NULL 
                     THEN 0 
                     ELSE CourseGroupMember.cgmGroupNumber END) as submitterUid
          FROM ClazzAssignment
               LEFT JOIN CourseGroupMember
               ON cgmSetUid = ClazzAssignment.caGroupUid
               AND cgmPersonUid = :personUid
         WHERE caUid = :assignmentUid
    """)
    abstract suspend fun getSubmitterUid(assignmentUid: Long, personUid: Long): Long

    @Update
    abstract suspend fun updateAsync(clazzAssignment: ClazzAssignment)

    @Query("""
        SELECT * 
          FROM ClazzAssignment 
         WHERE caUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): ClazzAssignment?


    @Query("""
        SELECT * 
          FROM ClazzAssignment 
         WHERE caUid = :uid
    """)
    abstract fun findByUidAsFlow(uid: Long): Flow<ClazzAssignment?>

    @Query("""
        SELECT EXISTS( 
               SELECT PrsGrpMbr.groupMemberPersonUid
                  FROM Clazz
                       ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                          :permission
                          ${Clazz.JOIN_FROM_SCOPEDGRANT_TO_PERSONGROUPMEMBER}
                 WHERE Clazz.clazzUid = 
                       (SELECT caClazzUid 
                          FROM ClazzAssignment
                         WHERE caUid = :clazzAssignmentUid)
                   AND PrsGrpMbr.groupMemberPersonUid = :accountPersonUid)
    """)
    abstract fun personHasPermissionWithClazzByAssignmentUidAsFlow(
        accountPersonUid: Long,
        clazzAssignmentUid: Long,
        permission: Long
    ): Flow<Boolean>

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

    @Query("""
        SELECT * 
          FROM ClazzAssignment
               LEFT JOIN CourseBlock
               ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
               AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
         WHERE caUid = :uid
    """)
    abstract suspend fun findByUidWithBlockAsync(uid: Long): ClazzAssignmentWithCourseBlock?

    @Query("""
        SELECT * 
          FROM ClazzAssignment LIMIT 1
    """)
    abstract fun findClazzAssignment(): ClazzAssignment?

    @Query("""SELECT * 
                      FROM ClazzAssignment 
                     WHERE caUid = :uid""")
    abstract fun findByUidLive(uid: Long): LiveData<ClazzAssignment?>


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

}