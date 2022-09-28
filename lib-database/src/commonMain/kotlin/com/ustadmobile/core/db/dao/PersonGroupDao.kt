package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.PersonGroupReplicate.Companion.PERSONGROUP_REPLICATE_NOT_ALREADY_UPDATE_SQL
import com.ustadmobile.lib.db.entities.PersonGroupReplicate.Companion.SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL
import com.ustadmobile.lib.db.entities.UserSession.Companion.USER_SESSION_NOT_LOCAL_DEVICE_SQL

@Repository
@DoorDao
expect abstract class PersonGroupDao : BaseDao<PersonGroup> {

    @Query("""
     REPLACE INTO PersonGroupReplicate(pgPk, pgDestination)
      SELECT DISTINCT $SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL,
             :newNodeId AS pgDestination
        FROM UserSession
             JOIN PersonGroupMember
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             JOIN PersonGroupMember PersonsWithPerm_GroupMember
                    ON PersonsWithPerm_GroupMember.groupMemberPersonUid = Person.personUid
             JOIN PersonGroup
                    ON PersonGroup.groupUid = PersonsWithPerm_GroupMember.groupMemberGroupUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND $PERSONGROUP_REPLICATE_NOT_ALREADY_UPDATE_SQL
      /*psql ON CONFLICT(pgPk, pgDestination) DO UPDATE
             SET pgPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO PersonGroupReplicate(pgPk, pgDestination)
  SELECT DISTINCT $SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL ,
         UserSession.usClientNodeId AS pgDestination
    FROM ChangeLog
         JOIN PersonGroup
              ON ChangeLog.chTableId = 43
                AND ChangeLog.chEntityPk = PersonGroup.groupUid
         JOIN PersonGroupMember
              ON PersonGroupMember.groupMemberGroupUid = PersonGroup.groupUid
         JOIN Person
              ON PersonGroupMember.groupMemberPersonUid = Person.personUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}                
   WHERE $USER_SESSION_NOT_LOCAL_DEVICE_SQL
     AND $PERSONGROUP_REPLICATE_NOT_ALREADY_UPDATE_SQL
 /*psql ON CONFLICT(pgPk, pgDestination) DO UPDATE
     SET pgPending = true
  */               
    """)
    @ReplicationRunOnChange([PersonGroup::class])
    @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
    abstract suspend fun replicateOnChange()

    @Query("""
 REPLACE INTO PersonGroupReplicate(pgPk, pgDestination)
       SELECT DISTINCT PersonGroup.groupUid AS pgUid,
              UserSession.usClientNodeId AS pgDestination
         FROM ChangeLog
              JOIN PersonGroup
                   ON ChangeLog.chTableId = ${PersonGroup.TABLE_ID}
                      AND ChangeLog.chEntityPk = PersonGroup.groupUid     
         JOIN ScopedGrant ScopedGrantEntity
              ON PersonGroup.groupUid = ScopedGrantEntity.sgGroupUid
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
          AND PersonGroup.groupLct != COALESCE(
              (SELECT pgVersionId
                 FROM PersonGroupReplicate
                WHERE pgPk = PersonGroup.groupUid
                  AND pgDestination = UserSession.usClientNodeId), 0)
    /*psql ON CONFLICT(pgPk, pgDestination) DO UPDATE
     SET pgPending = true
    */               
    """)
    @ReplicationRunOnChange([PersonGroup::class])
    @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
    abstract suspend fun replicateOnChangeClazzBased()


    @Query("""
 REPLACE INTO PersonGroupReplicate(pgPk, pgDestination)
  SELECT DISTINCT $SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL ,
         UserSession.usClientNodeId AS pgDestination
    FROM UserSession
         JOIN PersonGroupMember 
              ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
         ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_CLAZZ_SELECT} 
              ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
         JOIN ScopedGrant ScopedGrantEntity
              ON Clazz.clazzUid = ScopedGrantEntity.sgEntityUid
                 AND ScopedGrantEntity.sgTableId = ${Clazz.TABLE_ID}
         JOIN PersonGroup
              ON ScopedGrantEntity.sgGroupUid = PersonGroup.groupUid
   WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND PersonGroup.groupLct != COALESCE(
              (SELECT pgVersionId
                 FROM PersonGroupReplicate
                WHERE pgPk = PersonGroup.groupUid
                  AND pgDestination = UserSession.usClientNodeId), 0) 
      /*psql ON CONFLICT(pgPk, pgDestination) DO UPDATE
             SET pgPending = true
      */      
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
    abstract suspend fun replicateOnNewNodeClazzBased(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO PersonGroupReplicate(pgPk, pgDestination)
       SELECT DISTINCT PersonGroup.groupUid AS pgUid,
              UserSession.usClientNodeId AS pgDestination
         FROM ChangeLog
              JOIN PersonGroup
                   ON ChangeLog.chTableId = ${PersonGroup.TABLE_ID}
                      AND ChangeLog.chEntityPk = PersonGroup.groupUid     
         JOIN ScopedGrant ScopedGrantEntity
              ON PersonGroup.groupUid = ScopedGrantEntity.sgGroupUid
         JOIN Person 
              ON ScopedGrantEntity.sgTableId = ${Person.TABLE_ID}
                 AND ScopedGrantEntity.sgEntityUid = Person.personUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}                
        WHERE UserSession.usClientNodeId != (
                SELECT nodeClientId 
                  FROM SyncNode
                 LIMIT 1)
          AND PersonGroup.groupLct != COALESCE(
              (SELECT pgVersionId
                 FROM PersonGroupReplicate
                WHERE pgPk = PersonGroup.groupUid
                  AND pgDestination = UserSession.usClientNodeId), 0)
    /*psql ON CONFLICT(pgPk, pgDestination) DO UPDATE
     SET pgPending = true
    */                     
    """)
    @ReplicationRunOnChange([PersonGroup::class])
    @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
    abstract suspend fun replicateOnChangePersonBased()

    @Query("""
 REPLACE INTO PersonGroupReplicate(pgPk, pgDestination)
  SELECT DISTINCT $SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL ,
         UserSession.usClientNodeId AS pgDestination
    FROM UserSession
         JOIN PersonGroupMember 
              ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
         ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_SELECT}
              ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
         JOIN ScopedGrant ScopedGrantEntity
              ON Person.personUid = ScopedGrantEntity.sgEntityUid
                 AND ScopedGrantEntity.sgTableId = ${Person.TABLE_ID}
         JOIN PersonGroup
              ON ScopedGrantEntity.sgGroupUid = PersonGroup.groupUid
   WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND PersonGroup.groupLct != COALESCE(
              (SELECT pgVersionId
                 FROM PersonGroupReplicate
                WHERE pgPk = PersonGroup.groupUid
                  AND pgDestination = UserSession.usClientNodeId), 0) 
      /*psql ON CONFLICT(pgPk, pgDestination) DO UPDATE
             SET pgPending = true
      */      
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
    abstract suspend fun replicateOnNewNodePersonBased(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO PersonGroupReplicate(pgPk, pgDestination)
       SELECT DISTINCT PersonGroup.groupUid AS pgUid,
              UserSession.usClientNodeId AS pgDestination
         FROM ChangeLog
              JOIN PersonGroup
                   ON ChangeLog.chTableId = ${PersonGroup.TABLE_ID}
                      AND ChangeLog.chEntityPk = PersonGroup.groupUid     
         JOIN ScopedGrant ScopedGrantEntity
              ON PersonGroup.groupUid = ScopedGrantEntity.sgGroupUid
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
          AND PersonGroup.groupLct != COALESCE(
              (SELECT pgVersionId
                 FROM PersonGroupReplicate
                WHERE pgPk = PersonGroup.groupUid
                  AND pgDestination = UserSession.usClientNodeId), 0)
    /*psql ON CONFLICT(pgPk, pgDestination) DO UPDATE
     SET pgPending = true
    */                     
    """)
    @ReplicationRunOnChange([PersonGroup::class])
    @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
    abstract suspend fun replicateOnChangeSchoolBased()

    @Query("""
 REPLACE INTO PersonGroupReplicate(pgPk, pgDestination)
  SELECT DISTINCT $SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL ,
         UserSession.usClientNodeId AS pgDestination
    FROM UserSession
         JOIN PersonGroupMember 
              ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
         ${School.JOIN_FROM_PERSONGROUPMEMBER_TO_SCHOOL_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_SCHOOL_SELECT}
              ${School.JOIN_FROM_PERSONGROUPMEMBER_TO_SCHOOL_VIA_SCOPEDGRANT_PT2}
         JOIN ScopedGrant ScopedGrantEntity
              ON School.schoolUid = ScopedGrantEntity.sgEntityUid
                 AND ScopedGrantEntity.sgTableId = ${Person.TABLE_ID}
         JOIN PersonGroup
              ON ScopedGrantEntity.sgGroupUid = PersonGroup.groupUid
   WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND PersonGroup.groupLct != COALESCE(
              (SELECT pgVersionId
                 FROM PersonGroupReplicate
                WHERE pgPk = PersonGroup.groupUid
                  AND pgDestination = UserSession.usClientNodeId), 0) 
      /*psql ON CONFLICT(pgPk, pgDestination) DO UPDATE
             SET pgPending = true
      */      
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
    abstract suspend fun replicateOnNewNodeSchoolBased(@NewNodeIdParam newNodeId: Long)

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract fun findByUid(uid: Long): PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract fun findByUidLive(uid: Long): LiveData<PersonGroup?>


    @Update
    abstract suspend fun updateAsync(entity: PersonGroup) : Int

    @Query("""
        Select CASE
               WHEN Person.firstNames IS NOT NULL THEN Person.firstNames
               ELSE PersonGroup.groupName 
               END AS name
          FROM PersonGroup
               LEFT JOIN Person
                         ON Person.personGroupUid = PersonGroup.groupUid
         WHERE PersonGroup.groupUid = :groupUid
         LIMIT 1
    """)
    abstract suspend fun findNameByGroupUid(groupUid: Long): String?

}
