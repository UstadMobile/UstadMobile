package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Repository
@DoorDao
expect abstract class CourseGroupMemberDao: BaseDao<CourseGroupMember> {


    @Query("""
     REPLACE INTO CourseGroupMemberReplicate(cgmPk, cgmDestination)
      SELECT DISTINCT CourseGroupMember.cgmUid AS cgmUid,
             :newNodeId AS cgmDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
             JOIN CourseGroupSet
                    ON CourseGroupSet.cgsClazzUid = Clazz.clazzUid
             JOIN CourseGroupMember
                    ON CourseGroupMember.cgmSetUid = CourseGroupSet.cgsUid       
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND CourseGroupMember.cgmLct != COALESCE(
             (SELECT cgmVersionId
                FROM CourseGroupMemberReplicate
               WHERE cgmPk = CourseGroupMember.cgmUid
                 AND cgmDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(cgmPk, cgmDestination) DO UPDATE
             SET cgmPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CourseGroupMember::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO CourseGroupMemberReplicate(cgmPk, cgmDestination)
  SELECT DISTINCT CourseGroupMember.cgmUid AS cgmUid,
         UserSession.usClientNodeId AS cgmDestination
    FROM ChangeLog
         JOIN CourseGroupMember
               ON ChangeLog.chTableId = ${CourseGroupMember.TABLE_ID}
                  AND ChangeLog.chEntityPk = CourseGroupMember.cgmUid
          JOIN CourseGroupSet
               ON CourseGroupSet.cgsUid = CourseGroupMember.cgmSetUid       
          JOIN Clazz 
               ON Clazz.clazzUid = CourseGroupSet.cgsClazzUid 
          ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_CLAZZ_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND CourseGroupMember.cgmLct != COALESCE(
         (SELECT cgmVersionId
            FROM CourseGroupMemberReplicate
           WHERE cgmPk = CourseGroupMember.cgmUid
             AND cgmDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(cgmPk, cgmDestination) DO UPDATE
     SET cgmPending = true
  */               
 """)
    @ReplicationRunOnChange([CourseGroupMember::class])
    @ReplicationCheckPendingNotificationsFor([CourseGroupMember::class])
    abstract suspend fun replicateOnChange()

    @Query("""
        SELECT Person.*, CourseGroupMember.* 
          FROM Person
               JOIN ClazzEnrolment 
               ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
               AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
               AND ClazzEnrolment.clazzEnrolmentOutcome = ${ClazzEnrolment.OUTCOME_IN_PROGRESS}
               
               LEFT JOIN CourseGroupMember
               ON CourseGroupMember.cgmPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
               AND CourseGroupMember.cgmSetUid = :setUid
               
         WHERE clazzEnrolmentClazzUid = :clazzUid
      ORDER BY Person.firstNames
    """)
    abstract suspend fun findByGroupSetAsync(setUid: Long, clazzUid: Long): List<CourseGroupMemberPerson>

    @Query("""
        SELECT Person.*, CourseGroupMember.* 
          FROM Person
               JOIN ClazzEnrolment 
               ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
               AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT} 
               AND ClazzEnrolment.clazzEnrolmentOutcome = ${ClazzEnrolment.OUTCOME_IN_PROGRESS}
               
               LEFT JOIN CourseGroupMember
               ON CourseGroupMember.cgmPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
               AND CourseGroupMember.cgmSetUid = :setUid
               
         WHERE clazzEnrolmentClazzUid = :clazzUid
      ORDER BY CourseGroupMember.cgmGroupNumber, Person.firstNames
    """)
    abstract suspend fun findByGroupSetOrderedAsync(setUid: Long, clazzUid: Long): List<CourseGroupMemberPerson>

    @Query("""
        SELECT * 
          FROM CourseGroupMember
         WHERE cgmPersonUid = :studentUid 
          AND cgmSetUid = :groupSetUid
         LIMIT 1
    """)
    abstract suspend fun findByPersonUid(groupSetUid: Long, studentUid: Long): CourseGroupMember?

    @Insert
    abstract suspend fun insertListAsync(entityList: List<CourseGroupMember>)

    @Update
    abstract suspend fun updateListAsync(entityList: List<CourseGroupMember>)

    @Query("""
        --First get a list of all enrolments - this may contains duplicates for students who leave and re-enrol
        WITH AllEnrollmentsAndActiveStatus(enrolledPersonUid, isActive) AS 
             (SELECT ClazzEnrolment.clazzEnrolmentPersonUid AS enrolledPersonUid,
                     (:time BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft) AS isActive
                FROM ClazzEnrolment
               WHERE ClazzEnrolment.clazzEnrolmentClazzUid = 
                     -- If the courseGroupSet does not exist yet then we rely on the clazzUid param to find students
                     -- If the courseGroupSet does exist then we dont need the clazzUid
                     CASE(:clazzUid)
                         WHEN 0 THEN 
                                (SELECT CourseGroupSet.cgsClazzUid
                                   FROM CourseGroupSet
                                  WHERE CourseGroupSet.cgsUid = :cgsUid)
                         ELSE :clazzUid
                     END             
                 AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}),
        --Consolidate and removes any duplicates
             EnrolledStudentPersonUids(enrolledPersonUid, isActive) AS
             (SELECT DISTINCT AllEnrollmentsAndActiveStatus.enrolledPersonUid,
                     (SELECT CAST(AllEnrollmentsInner.isActive AS INTEGER)
                        FROM AllEnrollmentsAndActiveStatus AllEnrollmentsInner
                       WHERE AllEnrollmentsInner.enrolledPersonUid = AllEnrollmentsAndActiveStatus.enrolledPersonUid
                    ORDER BY AllEnrollmentsInner.isActive DESC
                       LIMIT 1) AS isActive
                FROM AllEnrollmentsAndActiveStatus)
        
        -- Now create a list with each students name, the coursegroupmember object if any and active status        
        SELECT (Person.firstNames || ' ' || Person.lastName) AS name,
               Person.personUid,
               CourseGroupMember.*,
               EnrolledStudentPersonUids.isActive AS enrolmentIsActive
          FROM EnrolledStudentPersonUids
               JOIN Person
                    ON Person.personUid = EnrolledStudentPersonUids.enrolledPersonUid 
               -- LEFT JOIN will use the most recent member in case of duplicate assignments eg if      
               LEFT JOIN CourseGroupMember
                         ON CourseGroupMember.cgmUid = 
                            (SELECT CourseGroupMember.cgmUid
                               FROM CourseGroupMember
                              WHERE CourseGroupMember.cgmPersonUid = EnrolledStudentPersonUids.enrolledPersonUid
                                AND CourseGroupMember.cgmSetUid = :cgsUid 
                           ORDER BY CourseGroupMember.cgmLct DESC        
                              LIMIT 1)

    """)
    /**
     * @param cgsUid CourseGroupSetUid - might be 0 if not created yet
     * @param clazzUid ClazzUid, required if the coursegroupset does not exist yet, otherwise may be 0
     * @param time the current time (used to determine if enrolments are active)
     */
    abstract suspend fun findByCourseGroupSetAndClazz(
        cgsUid: Long,
        clazzUid: Long,
        time: Long,
    ): List<CourseGroupMemberAndName>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(list: List<CourseGroupMember>)

}