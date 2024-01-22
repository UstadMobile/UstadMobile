package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL
import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon.ARG_FILTER_ALL_SCORES
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import app.cash.paging.PagingSource
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.composites.PersonAndPicture

@DoorDao
@Repository
expect abstract class CourseAssignmentMarkDao : BaseDao<CourseAssignmentMark> {


    @Query("""
        SELECT NOT EXISTS(SELECT 1
                        FROM CourseAssignmentMark
                       WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
                       LIMIT 1)
    """)
    abstract fun checkNoSubmissionsMarked(assignmentUid: Long): Flow<Boolean>

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
    @Deprecated("Will switch to using flow")
    abstract fun getMarkOfAssignmentForSubmitterLiveData(
        assignmentUid: Long,
        submitterUid: Long,
    ): Flow<AverageCourseAssignmentMark?>

    @HttpAccessible(

    )
    @Query("""
        SELECT CourseAssignmentMark.*,
               Person.firstNames AS markerFirstNames,
               Person.lastName AS markerLastName,
               PersonPicture.personPictureThumbnailUri AS markerPictureUri
          FROM CourseAssignmentMark
               LEFT JOIN Person
                         ON Person.personUid = CourseAssignmentMark.camMarkerPersonUid
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = CourseAssignmentMark.camMarkerPersonUid
         WHERE ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL) > 0
           AND CourseAssignmentMark.camAssignmentUid = :assignmentUid
           AND CourseAssignmentMark.camSubmitterUid = ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL)
      ORDER BY CourseAssignmentMark.camLct DESC    
    """)
    @QueryLiveTables(arrayOf("CourseAssignmentMark", "Person", "ClazzAssignment",
        "CourseGroupMember", "ClazzEnrolment", "PersonPicture"))
    abstract fun getAllMarksForUserAsFlow(
        accountPersonUid: Long,
        assignmentUid: Long
    ): Flow<List<CourseAssignmentMarkAndMarkerName>>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("getAllMarksForSubmitterAsFlow"),
            HttpServerFunctionCall("getAllMarksForSubmitterAsFlowMarkerPersons"),
        )
    )
    @Query("""
        SELECT CourseAssignmentMark.*,
               Person.firstNames AS markerFirstNames,
               Person.lastName AS markerLastName,
               PersonPicture.personPictureThumbnailUri AS markerPictureUri
          FROM CourseAssignmentMark
               LEFT JOIN Person
                         ON Person.personUid = CourseAssignmentMark.camMarkerPersonUid
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = CourseAssignmentMark.camMarkerPersonUid
         WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
           AND CourseAssignmentMark.camSubmitterUid = :submitterUid
      ORDER BY CourseAssignmentMark.camLct DESC                             
    """)
    abstract fun getAllMarksForSubmitterAsFlow(
        submitterUid: Long,
        assignmentUid: Long,
    ): Flow<List<CourseAssignmentMarkAndMarkerName>>

    @Query("""
        SELECT Person.*
          FROM Person
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = Person.personUid
         WHERE PersonUid IN
               (SELECT CourseAssignmentMark.camMarkerPersonUid
                  FROM CourseAssignmentMark
                 WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
                   AND CourseAssignmentMark.camSubmitterUid = :submitterUid)
    """)
    abstract suspend fun getAllMarksForSubmitterAsFlowMarkerPersons(
        submitterUid: Long,
        assignmentUid: Long,
    ): List<PersonAndPicture>

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
    ): PagingSource<Int, CourseAssignmentMarkWithPersonMarker>

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