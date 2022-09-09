package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Repository
@DoorDao
expect abstract class PersonGroupMemberDao : BaseDao<PersonGroupMember> {

    /**
     * PersonGroupMember must replicate to:
     *  1) Anyone who has select permission for the person linked to the member
     *  2) If this PersonGroupMember is related to a group that is used with ScopedGrant(s), then it
     *     must be replicated for anyone who has permission to select the subject (e.g. Clazz, School,
     *      Person, etc) of the ScopedGrant.
     */

    @Query("""
     REPLACE INTO PersonGroupMemberReplicate(pgmPk, pgmDestination)
      SELECT DISTINCT PersonGroupMember.groupMemberUid AS pgmUid,
             :newNodeId AS pgmDestination
        FROM UserSession
             JOIN PersonGroupMember
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             JOIN PersonGroupMember PersonsWithPerm_GroupMember
                    ON PersonsWithPerm_GroupMember.groupMemberPersonUid = Person.personUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND PersonGroupMember.groupMemberLct != COALESCE(
             (SELECT pgmVersionId
                FROM PersonGroupMemberReplicate
               WHERE pgmPk = PersonGroupMember.groupMemberUid
                 AND pgmDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(pgmPk, pgmDestination) DO UPDATE
             SET pgmPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonGroupMember::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO PersonGroupMemberReplicate(pgmPk, pgmDestination)
  SELECT DISTINCT PersonGroupMember.groupMemberUid AS pgmUid,
         UserSession.usClientNodeId AS pgmDestination
    FROM ChangeLog
         JOIN PersonGroupMember
             ON ChangeLog.chTableId = ${PersonGroupMember.TABLE_ID}
                AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
         JOIN Person
              ON PersonGroupMember.groupMemberPersonUid = Person.personUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND PersonGroupMember.groupMemberLct != COALESCE(
         (SELECT pgmVersionId
            FROM PersonGroupMemberReplicate
           WHERE pgmPk = PersonGroupMember.groupMemberUid
             AND pgmDestination = UserSession.usClientNodeId), 0)
     /*psql ON CONFLICT(pgmPk, pgmDestination) DO UPDATE
         SET pgmPending = true
      */               
     """)
    @ReplicationRunOnChange([PersonGroupMember::class])
    @ReplicationCheckPendingNotificationsFor([PersonGroupMember::class])
    abstract suspend fun replicateOnChange()

    @Query("""
 REPLACE INTO PersonGroupMemberReplicate(pgmPk, pgmDestination)
  SELECT DISTINCT PersonGroupMember.groupMemberUid AS pgmUid,
         UserSession.usClientNodeId AS pgmDestination
    FROM ChangeLog
         JOIN PersonGroupMember
             ON ChangeLog.chTableId = ${PersonGroupMember.TABLE_ID}
                AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
         JOIN ScopedGrant ScopedGrantEntity
              ON PersonGroupMember.groupMemberUid = ScopedGrantEntity.sgGroupUid
         JOIN Clazz 
              ON ScopedGrantEntity.sgTableId = ${Clazz.TABLE_ID}
                 AND ScopedGrantEntity.sgEntityUid = Clazz.clazzUid
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_CLAZZ_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}      
   WHERE UserSession.usClientNodeId != (
                SELECT nodeClientId 
                  FROM SyncNode
                 LIMIT 1)
     AND PersonGroupMember.groupMemberLct != COALESCE(
              (SELECT pgmVersionId
                 FROM PersonGroupMemberReplicate
                WHERE pgmPk = PersonGroupMember.groupMemberUid
                  AND pgmDestination = UserSession.usClientNodeId), 0)
  /*psql ON CONFLICT(pgmPk, pgmDestination) DO UPDATE
     SET pgmPending = true
    */                   
    """)
    @ReplicationRunOnChange([PersonGroupMember::class])
    @ReplicationCheckPendingNotificationsFor([PersonGroupMember::class])
    abstract suspend fun replicateOnChangeClazzBased()

    @Query("""
 REPLACE INTO PersonGroupMemberReplicate(pgmPk, pgmDestination)
      SELECT DISTINCT PersonGroupMember.groupMemberUid AS pgmUid,
             :newNodeId AS pgmDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_CLAZZ_SELECT} 
                  ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
             JOIN ScopedGrant ScopedGrantEntity
                  ON Clazz.clazzUid = ScopedGrantEntity.sgEntityUid
                     AND ScopedGrantEntity.sgTableId = ${Clazz.TABLE_ID}
             JOIN PersonGroupMember PersonGroupMemberEntity
                  ON PersonGroupMemberEntity.groupMemberGroupUid = ScopedGrantEntity.sgGroupUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}  
         AND PersonGroupMember.groupMemberLct != COALESCE(
             (SELECT pgmVersionId
                FROM PersonGroupMemberReplicate
               WHERE pgmPk = PersonGroupMember.groupMemberUid
                 AND pgmDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(pgmPk, pgmDestination) DO UPDATE
             SET pgmPending = true
      */                
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonGroupMember::class])
    abstract suspend fun replicateOnNewNodeClazzBased(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO PersonGroupMemberReplicate(pgmPk, pgmDestination)
  SELECT DISTINCT PersonGroupMember.groupMemberUid AS pgmUid,
         UserSession.usClientNodeId AS pgmDestination
    FROM ChangeLog
         JOIN PersonGroupMember
             ON ChangeLog.chTableId = ${PersonGroupMember.TABLE_ID}
                AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
         JOIN ScopedGrant ScopedGrantEntity
              ON PersonGroupMember.groupMemberUid = ScopedGrantEntity.sgGroupUid
         JOIN School 
              ON ScopedGrantEntity.sgTableId = ${School.TABLE_ID}
                 AND ScopedGrantEntity.sgEntityUid = School.schoolUid
         ${School.JOIN_FROM_SCHOOL_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_SCHOOL_SELECT}
              ${School.JOIN_FROM_SCHOOL_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
                SELECT nodeClientId 
                  FROM SyncNode
                 LIMIT 1)
     AND PersonGroupMember.groupMemberLct != COALESCE(
              (SELECT pgmVersionId
                 FROM PersonGroupMemberReplicate
                WHERE pgmPk = PersonGroupMember.groupMemberUid
                  AND pgmDestination = UserSession.usClientNodeId), 0)
  /*psql ON CONFLICT(pgmPk, pgmDestination) DO UPDATE
     SET pgmPending = true
    */                   
    """)
    @ReplicationRunOnChange([PersonGroupMember::class])
    @ReplicationCheckPendingNotificationsFor([PersonGroupMember::class])
    abstract suspend fun replicateOnChangeSchoolBased()

    @Query("""
 REPLACE INTO PersonGroupMemberReplicate(pgmPk, pgmDestination)
      SELECT DISTINCT PersonGroupMember.groupMemberUid AS pgmUid,
             :newNodeId AS pgmDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${School.JOIN_FROM_PERSONGROUPMEMBER_TO_SCHOOL_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_SCHOOL_SELECT}
                  ${School.JOIN_FROM_PERSONGROUPMEMBER_TO_SCHOOL_VIA_SCOPEDGRANT_PT2}
             JOIN ScopedGrant ScopedGrantEntity
                  ON School.schoolUid = ScopedGrantEntity.sgEntityUid
                     AND ScopedGrantEntity.sgTableId = ${School.TABLE_ID}
             JOIN PersonGroupMember PersonGroupMemberEntity
                  ON PersonGroupMemberEntity.groupMemberGroupUid = ScopedGrantEntity.sgGroupUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}  
         AND PersonGroupMember.groupMemberLct != COALESCE(
             (SELECT pgmVersionId
                FROM PersonGroupMemberReplicate
               WHERE pgmPk = PersonGroupMember.groupMemberUid
                 AND pgmDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(pgmPk, pgmDestination) DO UPDATE
             SET pgmPending = true
      */                
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonGroupMember::class])
    abstract suspend fun replicateOnNewNodeSchoolBased(@NewNodeIdParam newNodeId: Long)


    @Query("SELECT * FROM PersonGroupMember WHERE groupMemberPersonUid = :personUid " +
            "AND PersonGroupMember.groupMemberActive")
    abstract suspend fun findAllGroupWherePersonIsIn(personUid: Long) : List<PersonGroupMember>

    @Query("""SELECT * FROM PersonGroupMember WHERE groupMemberGroupUid = :groupUid 
             AND groupMemberPersonUid = :personUid AND PersonGroupMember.groupMemberActive""" )
    abstract suspend fun checkPersonBelongsToGroup(groupUid: Long, personUid: Long): List<PersonGroupMember>

    /**
     * Updates an existing group membership to a new group
     */
    @Query("""
        UPDATE PersonGroupMember 
           SET groupMemberGroupUid = :newGroup,
               groupMemberLct = :changeTime
         WHERE groupMemberPersonUid = :personUid 
           AND groupMemberGroupUid = :oldGroup 
           AND PersonGroupMember.groupMemberActive""")
    abstract suspend fun moveGroupAsync(
        personUid: Long,
        newGroup: Long,
        oldGroup: Long,
        changeTime: Long
    ): Int

    @Query("""
        UPDATE PersonGroupMember 
           SET groupMemberActive = :activeStatus,
               groupMemberLct = :updateTime
        WHERE groupMemberPersonUid = :personUid 
          AND groupMemberGroupUid = :groupUid 
          AND PersonGroupMember.groupMemberActive""")
    abstract suspend fun updateGroupMemberActive(
        activeStatus: Boolean,
        personUid: Long,
        groupUid: Long,
        updateTime: Long
    )

    @Query("""
        SELECT PersonGroupMember.*
          FROM PersonGroupMember
         WHERE PersonGroupMember.groupMemberPersonUid = :personUid
           AND PersonGroupMember.groupMemberGroupUid = :groupUid
    """)
    abstract suspend fun findByPersonUidAndGroupUid(personUid: Long, groupUid: Long): PersonGroupMember?

}
