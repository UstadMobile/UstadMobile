package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Repository
@Dao
abstract class CourseGroupMemberDao: BaseDao<CourseGroupMember> {


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

}