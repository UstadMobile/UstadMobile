package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@DoorDao
expect abstract class StudentResultDao {

    @Query("""
     REPLACE INTO StudentResultReplicate(srPk, srDestination)
      SELECT DISTINCT StudentResult.srUid AS srPk,
             :newNodeId AS srDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}
                  ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             JOIN StudentResult
                  ON StudentResult.srStudentPersonUid = Person.personUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND StudentResult.srLastModified != COALESCE(
             (SELECT srVersionId
                FROM StudentResultReplicate
               WHERE srPk = StudentResult.srUid
                 AND srDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(srPk, srDestination) DO UPDATE
             SET srPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([StudentResult::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO StudentResultReplicate(srPk, srDestination)
  SELECT DISTINCT STudentResult.srUid AS srUid,
         UserSession.usClientNodeId AS srDestination
    FROM ChangeLog
         JOIN StudentResult
              ON ChangeLog.chTableId = ${StudentResult.TABLE_ID}
                 AND ChangeLog.chEntityPk = StudentResult.srUid
         JOIN Person
              ON Person.personUid = StudentResult.srStudentPersonUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_PICTURE_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND StudentResult.srLastModified != COALESCE(
         (SELECT srVersionId
            FROM StudentResultReplicate
           WHERE srPk = StudentResult.srUid
             AND srDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(srPk, srDestination) DO UPDATE
     SET srPending = true
  */               
    """)
    @ReplicationRunOnChange([StudentResult::class])
    @ReplicationCheckPendingNotificationsFor([StudentResult::class])
    abstract suspend fun replicateOnChange()

    /**
     *
     */
    @Query("""
        SELECT StudentResult.*,
               CourseBlock.cbSourcedId AS cbSourcedId
          FROM StudentResult
               LEFT JOIN CourseBlock
                         ON StudentResult.srCourseBlockUid = CourseBlock.cbUid 
         WHERE StudentResult.srClazzUid = :clazzUid
           AND StudentResult.srStudentPersonUid = :studentPersonUid
           AND EXISTS(
               SELECT ScopedGrant.sgUid 
                  FROM ScopedGrant
                       JOIN PersonGroupMember
                            ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                            
                 WHERE /* ScopedGrant scope must match the class or person */
                       ((ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                           AND ScopedGrant.sgEntityUid = StudentResult.srClazzUid)
                       OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                               AND ScopedGrant.sgEntityUid = StudentResult.srStudentPersonUid)
                       OR (ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}))
                       /* ScopedGrant must provide learning record select permission */
                   AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}) = ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}
                       /* ScopedGrant must be granted to the person as per accountPersonUid */
                   AND PersonGroupMember.groupMemberPersonUid = :accountPersonUid
               )
    """)
    abstract suspend fun findByClazzAndStudent(
        clazzUid: Long,
        studentPersonUid: Long,
        accountPersonUid: Long
    ): List<StudentResultAndSourcedIds>


    @Query("""
        SELECT EXISTS(
               SELECT srUid
                 FROM StudentResult
                WHERE srSourcedId = :sourcedId 
               )
    """)
    abstract suspend fun sourcedUidExists(
        sourcedId: String
    ): Boolean

    @Insert
    abstract suspend fun insertListAsync(results: List<StudentResult>)

}