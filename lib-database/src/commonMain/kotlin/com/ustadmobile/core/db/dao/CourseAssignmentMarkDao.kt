package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon.ARG_FILTER_ALL_SCORES
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.door.paging.DataSourceFactory

@DoorDao
@Repository
expect abstract class CourseAssignmentMarkDao : BaseDao<CourseAssignmentMark> {


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
    abstract fun checkNoSubmissionsMarked(assignmentUid: Long): LiveData<Boolean>

    @Query("""
         WITH ScoreByMarker (averageScore, averagePenalty) AS (
                 SELECT AVG(camMark), AVG(camPenalty)
                   FROM courseAssignmentMark
                        JOIN ClazzAssignment
                        ON caUid = courseAssignmentMark.camAssignmentUid         
                    AND camAssignmentUid = :assignmentUid
                    AND camSubmitterUid = :submitterUid
                  WHERE camLct = (SELECT MAX(mark.camLct) 
                                    FROM CourseAssignmentMark As mark
                                    WHERE mark.camAssignmentUid = :assignmentUid
                                     AND mark.camSubmitterUid = :submitterUid
                                     AND (caMarkingType = ${ClazzAssignment.MARKED_BY_COURSE_LEADER}
                                       OR mark.camMarkerSubmitterUid = courseAssignmentMark.camMarkerSubmitterUid))
                )                       

         SELECT COALESCE(averageScore, -1) AS averageScore, COALESCE(averagePenalty, -1) AS averagePenalty
           FROM ScoreByMarker
    """)
    abstract fun getMarkOfAssignmentForSubmitterLiveData(
        assignmentUid: Long,
        submitterUid: Long,
    ): LiveData<AverageCourseAssignmentMark?>

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
                
          SELECT marker.*, ScoreByMarker.*, (ClazzAssignment.caGroupUid != 0) AS isGroup
            FROM ScoreByMarker
                 JOIN Person As marker
                 ON Marker.personUid = ScoreByMarker.camMarkerPersonUid
                 JOIN ClazzAssignment
                 ON ClazzAssignment.caUid = :assignmentUid
    """)
    @QueryLiveTables(value = ["courseAssignmentMark","ClazzAssignment"])
    abstract fun getAllMarksOfAssignmentForSubmitter(
        assignmentUid: Long,
        submitterUid: Long,
        filter: Int
    ): DataSourceFactory<Int, CourseAssignmentMarkWithPersonMarker>

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
              
                   JOIN ClazzAssignment
                   ON ClazzAssignment.caUid = CourseAssignmentSubmission.casAssignmentUid
              
                   LEFT JOIN CourseAssignmentMark
                   ON CourseAssignmentMark.camSubmitterUid = CourseAssignmentSubmission.casSubmitterUid
                   AND CourseAssignmentMark.camAssignmentUid = ClazzAssignment.caUid
                   
                   LEFT JOIN PeerReviewerAllocation
                   ON praAssignmentUid = ClazzAssignment.caUid
                   AND praToMarkerSubmitterUid = :submitterUid
                   
             WHERE CourseAssignmentSubmission.casSubmitterUid != :submitterUid
               AND CourseAssignmentSubmission.casSubmitterUid != :markerUid
               AND CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
               AND CourseAssignmentMark.camUid IS NULL
               AND (ClazzAssignment.caMarkingType = ${ClazzAssignment.MARKED_BY_COURSE_LEADER} 
                    OR PeerReviewerAllocation.praMarkerSubmitterUid = :markerUid)
          GROUP BY casSubmitterUid
         LIMIT 1),0)
    """)
    abstract suspend fun findNextSubmitterToMarkForAssignment(assignmentUid: Long,
                                                              submitterUid: Long,
                                                              markerUid: Long): Long



}