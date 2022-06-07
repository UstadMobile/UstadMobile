package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Dao
@Repository
abstract class CourseAssignmentMarkDao : BaseDao<CourseAssignmentMark> {


    @Query("""
     REPLACE INTO CourseAssignmentMarkReplicate(camPk, camDestination)
      SELECT DISTINCT CourseAssignmentMark.camUid AS camPk,
             :newNodeId AS camDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
               JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = Clazz.clazzUid                
             JOIN CourseAssignmentMark
                    ON CourseAssignmentMark.camAssignmentUid = ClazzAssignment.caUid
                    
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND CourseAssignmentMark.camLct != COALESCE(
             (SELECT camVersionId
                FROM CourseAssignmentMarkReplicate
               WHERE camPk = CourseAssignmentMark.camUid
                 AND camDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(camPk, camDestination) DO UPDATE
             SET camPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CourseAssignmentMark::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO CourseAssignmentMarkReplicate(camPk, camDestination)
  SELECT DISTINCT CourseAssignmentMark.camUid AS camPk,
         UserSession.usClientNodeId AS camDestination
    FROM ChangeLog
         JOIN CourseAssignmentMark
             ON ChangeLog.chTableId = ${CourseAssignmentMark.TABLE_ID}
                AND ChangeLog.chEntityPk = CourseAssignmentMark.camUid
             JOIN ClazzAssignment
                    ON CourseAssignmentMark.camAssignmentUid = ClazzAssignment.caUid
             JOIN Clazz
                    ON  Clazz.clazzUid = ClazzAssignment.caClazzUid
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_ASSIGNMENT_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND CourseAssignmentMark.camLct != COALESCE(
         (SELECT camVersionId
            FROM CourseAssignmentMarkReplicate
           WHERE camPk = CourseAssignmentMark.camUid
             AND camDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(camPk, camDestination) DO UPDATE
     SET camPending = true
  */               
 """)
    @ReplicationRunOnChange([CourseAssignmentMark::class])
    @ReplicationCheckPendingNotificationsFor([CourseAssignmentMark::class])
    abstract suspend fun replicateOnChange()


    @Query("""
        SELECT NOT EXISTS(SELECT 1
                        FROM CourseAssignmentMark
                       WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
                       LIMIT 1)
    """)
    abstract fun checkNoSubmissionsMarked(assignmentUid: Long): DoorLiveData<Boolean>

    @Query("""
         WITH ScoreByMarker (score, penalty) AS (
                 SELECT AVG(camMark), AVG(camPenalty)
                   FROM courseAssignmentMark
                        JOIN ClazzAssignment
                        ON caUid = :assignmentUid         
                    AND camAssignmentUid = :assignmentUid
                    AND camSubmitterUid = :submitterUid
                  WHERE camLct = (SELECT MAX(mark.camLct) 
                                    FROM CourseAssignmentMark As mark
                                    WHERE mark.camAssignmentUid = :assignmentUid
                                     AND mark.camSubmitterUid = :submitterUid
                                     AND (caMarkingType = ${ClazzAssignment.MARKED_BY_COURSE_LEADER}
                                       OR mark.camMarkerSubmitterUid = courseAssignmentMark.camMarkerSubmitterUid))
                 GROUP BY camMarkerSubmitterUid                                                                           
                ORDER BY camLct DESC)                       
                                       
      
         SELECT *
           FROM ScoreByMarker
    """)
    abstract fun getMarkOfAssignmentForSubmitterLiveData(
        assignmentUid: Long,
        submitterUid: Long,
    ): DoorLiveData<AverageCourseAssignmentMark?>

    @Query("""
          WITH ScoreByMarker AS (
                 SELECT *
                   FROM courseAssignmentMark    
                  WHERE camAssignmentUid = :assignmentUid
                    AND camSubmitterUid = :submitterUid
                    AND (:filter = $ARG_FILTER_ALL_SCORES OR camLct = (SELECT MAX(mark.camLct) 
                                    FROM CourseAssignmentMark As mark
                                    WHERE mark.camAssignmentUid = :assignmentUid
                                      AND mark.camSubmitterUid = :submitterUid
                                      AND mark.camMarkerSubmitterUid = courseAssignmentMark.camMarkerSubmitterUid
                                      ))                 
                ORDER BY camLct DESC)    
                
          SELECT * 
            FROM ScoreByMarker
                 JOIN Person As marker
                 ON Marker.personUid = ScoreByMarker.camMarkerPersonUid
    """)
    @QueryLiveTables(value = ["courseAssignmentMark","ClazzAssignment"])
    abstract fun getAllMarksOfAssignmentForSubmitter(
        assignmentUid: Long,
        submitterUid: Long,
        filter: Int
    ): DoorDataSourceFactory<Int, CourseAssignmentMarkWithPersonMarker>

    @Query("""
        SELECT * 
          FROM CourseAssignmentMark
         WHERE camAssignmentUid = :assignmentUid
           AND camSubmitterUid = :submitterUid
      ORDER BY camLct DESC
         LIMIT 1
    """)
    abstract fun getMarkOfAssignmentForStudent(assignmentUid: Long, submitterUid: Long): CourseAssignmentMark?


    @Query("""
         SELECT COALESCE((
            SELECT casSubmitterUid
              FROM CourseAssignmentSubmission
              
                   LEFT JOIN CourseAssignmentMark
                   ON CourseAssignmentMark.camSubmitterUid = CourseAssignmentSubmission.casSubmitterUid
                   AND CourseAssignmentMark.camAssignmentUid = :assignmentUid
                   
                   LEFT JOIN PeerReviewerAllocation
                   ON praAssignmentUid = :assignmentUid
                   AND praToMarkerSubmitterUid = :submitterUid
                   
                   JOIN ClazzAssignment
                   ON ClazzAssignment.caUid = :assignmentUid
                   
             WHERE CourseAssignmentSubmission.casSubmitterUid != :submitterUid
               AND CourseAssignmentSubmission.casSubmitterUid != :markerUid
               AND CourseAssignmentMark.camUid IS NULL
               AND (ClazzAssignment.caMarkingType = ${ClazzAssignment.MARKED_BY_COURSE_LEADER} 
                    OR PeerReviewerAllocation.praMarkerSubmitterUid = :markerUid)
          GROUP BY casSubmitterUid
         LIMIT 1),0)
    """)
    abstract suspend fun findNextSubmitterToMarkForAssignment(assignmentUid: Long,
                                                              submitterUid: Long,
                                                              markerUid: Long): Long


    companion object{

        const val ARG_FILTER_RECENT_SCORES = 1

        const val ARG_FILTER_ALL_SCORES = 0

    }
}