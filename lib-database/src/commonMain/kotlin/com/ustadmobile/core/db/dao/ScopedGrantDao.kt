package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Dao
@Repository
abstract class ScopedGrantDao {

    /**
     * ScopedGrant must replicate to:
     *   1) Anyone who has person select permission for anyone that is the group to which the
     *      ScopedGrant belongs.
     *   2) Anyone who has permission to select the underlying entity (e.g. Person, Class, School).
     *      This is important because 1) will not lead to replication when the group itself starts
     *      out empty.
     */

    @Query("""
     REPLACE INTO ScopedGrantReplicate(sgPk, sgDestination)
      SELECT DISTINCT ScopedGrantWithPerm.sgUid AS sgPk,
             :newNodeId AS sgDestination
        FROM UserSession
             JOIN PersonGroupMember
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             JOIN PersonGroupMember PersonsWithPerm_GroupMember
                    ON PersonsWithPerm_GroupMember.groupMemberPersonUid = Person.personUid
             JOIN ScopedGrant ScopedGrantWithPerm
                    ON PersonsWithPerm_GroupMember.groupMemberGroupUid = ScopedGrantWithPerm.sgGroupUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND ScopedGrantWithPerm.sgLct != COALESCE(
             (SELECT sgVersionId
                FROM ScopedGrantReplicate
               WHERE sgPk = ScopedGrantWithPerm.sgUid
                 AND sgDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(sgPk, sgDestination) DO UPDATE
             SET sgPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ScopedGrant::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ScopedGrantReplicate(sgPk, sgDestination)
  SELECT DISTINCT ScopedGrantEntity.sgUid AS sgPk,
         UserSession.usClientNodeId AS sgDestination
    FROM ChangeLog
         JOIN ScopedGrant ScopedGrantEntity
             ON ChangeLog.chTableId = 48
                AND ChangeLog.chEntityPk = ScopedGrantEntity.sgUid
         JOIN PersonGroupMember
              ON PersonGroupMember.groupMemberGroupUid = ScopedGrantEntity.sgGroupUid
         JOIN Person
              ON PersonGroupMember.groupMemberPersonUid = Person.personUid
         ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_PERSON_SELECT}
              ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ScopedGrantEntity.sgLct != COALESCE(
         (SELECT sgVersionId
            FROM ScopedGrantReplicate
           WHERE sgPk = ScopedGrantEntity.sgUid
             AND sgDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(sgPk, sgDestination) DO UPDATE
     SET sgPending = true
  */               
    """)
    @ReplicationRunOnChange([ScopedGrant::class])
    @ReplicationCheckPendingNotificationsFor([ScopedGrant::class])
    abstract suspend fun replicateOnChange()

    @Query("""
 REPLACE INTO ScopedGrantReplicate(sgPk, sgDestination)
  SELECT DISTINCT ScopedGrantEntity.sgUid AS sgPk,
         UserSession.usClientNodeId AS sgDestination
    FROM ChangeLog
         JOIN ScopedGrant ScopedGrantEntity
              ON ChangeLog.chTableId = 48
                 AND ChangeLog.chEntityPk = ScopedGrantEntity.sgUid
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
     AND ScopedGrantEntity.sgLct != COALESCE(
         (SELECT sgVersionId
            FROM ScopedGrantReplicate
           WHERE sgPk = ScopedGrantEntity.sgUid
             AND sgDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(sgPk, sgDestination) DO UPDATE
     SET sgPending = true
  */               
    """)
    @ReplicationRunOnChange([ScopedGrant::class])
    @ReplicationCheckPendingNotificationsFor([ScopedGrant::class])
    abstract suspend fun replicateOnChangeClazzBased()

    @Query("""
     REPLACE INTO ScopedGrantReplicate(sgPk, sgDestination)
      SELECT DISTINCT ScopedGrantEntity.sgUid AS sgPk,
             :newNodeId AS sgDestination
        FROM UserSession
               JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
               JOIN ScopedGrant ScopedGrantEntity
                    ON Clazz.clazzUid = ScopedGrant.sgEntityUid
                       AND ScopedGrantEntity.sgTableId = ${Clazz.TABLE_ID}
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND ScopedGrantEntity.sgLct != COALESCE(
             (SELECT sgVersionId
                FROM ScopedGrantReplicate
               WHERE sgPk = ScopedGrantEntity.sgUid
                 AND sgDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(sgPk, sgDestination) DO UPDATE
             SET sgPending = true
      */
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ScopedGrant::class])
    abstract suspend fun replicateOnNewNodeClazzBased(newNodeId: Long)

    @Query("""
 REPLACE INTO ScopedGrantReplicate(sgPk, sgDestination)
  SELECT DISTINCT ScopedGrantEntity.sgUid AS sgPk,
         UserSession.usClientNodeId AS sgDestination
    FROM ChangeLog
         JOIN ScopedGrant ScopedGrantEntity
              ON ChangeLog.chTableId = ${ScopedGrant.TABLE_ID}
                 AND ChangeLog.chEntityPk = ScopedGrantEntity.sgUid
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
     AND ScopedGrantEntity.sgLct != COALESCE(
         (SELECT sgVersionId
            FROM ScopedGrantReplicate
           WHERE sgPk = ScopedGrantEntity.sgUid
             AND sgDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(sgPk, sgDestination) DO UPDATE
     SET sgPending = true
  */                    
    """)
    @ReplicationRunOnChange([ScopedGrant::class])
    @ReplicationCheckPendingNotificationsFor([ScopedGrant::class])
    abstract suspend fun replicateOnChangePersonBased()


    @Query("""
 REPLACE INTO ScopedGrantReplicate(sgPk, sgDestination)
      SELECT DISTINCT ScopedGrantEntity.sgUid AS sgPk,
             :newNodeId AS sgDestination
        FROM UserSession
               JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
               JOIN ScopedGrant ScopedGrantEntity
                    ON ScopedGrantEntity.sgTableId = ${Person.TABLE_ID}
                       AND ScopedGrantEntity.sgEntityUid = Person.personUid 
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE} 
         AND ScopedGrantEntity.sgLct != COALESCE(
             (SELECT sgVersionId
                FROM ScopedGrantReplicate
               WHERE sgPk = ScopedGrantEntity.sgUid
                 AND sgDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(sgPk, sgDestination) DO UPDATE
     SET sgPending = true
  */                                                       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ScopedGrant::class])
    abstract suspend fun replicateOnNewNodePersonBased(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ScopedGrantReplicate(sgPk, sgDestination)
  SELECT DISTINCT ScopedGrantEntity.sgUid AS sgPk,
         UserSession.usClientNodeId AS sgDestination
    FROM ChangeLog
         JOIN ScopedGrant ScopedGrantEntity
              ON ChangeLog.chTableId = ${ScopedGrant.TABLE_ID}
                 AND ChangeLog.chEntityPk = ScopedGrantEntity.sgUid
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
     AND ScopedGrantEntity.sgLct != COALESCE(
             (SELECT sgVersionId
                FROM ScopedGrantReplicate
               WHERE sgPk = ScopedGrantEntity.sgUid
                 AND sgDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(sgPk, sgDestination) DO UPDATE
     SET sgPending = true
  */                 
    """)
    @ReplicationRunOnChange([ScopedGrant::class])
    @ReplicationCheckPendingNotificationsFor([ScopedGrant::class])
    abstract suspend fun replicateOnChangeSchoolBased()

    @Query("""
 REPLACE INTO ScopedGrantReplicate(sgPk, sgDestination)
      SELECT DISTINCT ScopedGrantEntity.sgUid AS sgPk,
             :newNodeId AS sgDestination
        FROM UserSession
               JOIN PersonGroupMember
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               ${School.JOIN_FROM_PERSONGROUPMEMBER_TO_SCHOOL_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_SCHOOL_SELECT}
                    ${School.JOIN_FROM_PERSONGROUPMEMBER_TO_SCHOOL_VIA_SCOPEDGRANT_PT2}
               JOIN ScopedGrant ScopedGrantEntity
                    ON ScopedGrantEntity.sgTableId = ${School.TABLE_ID}
                       AND ScopedGrantEntity.sgEntityUid = School.schoolUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE} 
         AND ScopedGrantEntity.sgLct != COALESCE(
             (SELECT sgVersionId
                FROM ScopedGrantReplicate
               WHERE sgPk = ScopedGrantEntity.sgUid
                 AND sgDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(sgPk, sgDestination) DO UPDATE
     SET sgPending = true
  */                                                                                 
    """)
    abstract suspend fun replicateOnNewNodeSchoolBased(@NewNodeIdParam newNodeId: Long)

    @Insert
    abstract suspend fun insertAsync(scopedGrant: ScopedGrant): Long

    @Insert
    abstract suspend fun insertListAsync(scopedGrantList: List<ScopedGrant>)

    @Update
    abstract suspend fun updateAsync(scopedGrant: ScopedGrant)

    @Update
    abstract suspend fun updateListAsync(scopedGrantList: List<ScopedGrant>)

    @Query(SQL_FIND_BY_TABLE_AND_ENTITY)
    abstract suspend fun findByTableIdAndEntityUid(tableId: Int, entityUid: Long): List<ScopedGrantAndName>

    @Query(SQL_FIND_BY_TABLE_AND_ENTITY)
    abstract fun findByTableIdAndEntityUidWithNameAsDataSource(
        tableId: Int,
        entityUid: Long
    ): DoorDataSourceFactory<Int, ScopedGrantWithName>


    @Query("""
        SELECT ScopedGrant.*
          FROM ScopedGrant
         WHERE sgTableId = :tableId
           AND sgEntityUid = :entityUid
    """)
    abstract fun findByTableIdAndEntityIdSync(tableId: Int, entityUid: Long): List<ScopedGrant>

    @Query("""
        SELECT ScopedGrant.*
          FROM ScopedGrant
         WHERE sgUid = :sgUid 
    """)
    abstract suspend fun findByUid(sgUid: Long): ScopedGrant?

    @Query("""
        SELECT ScopedGrant.*, 
               CASE
               WHEN Person.firstNames IS NOT NULL THEN Person.firstNames
               ELSE PersonGroup.groupName 
               END AS name
          FROM ScopedGrant
               LEFT JOIN PersonGroup 
                    ON ScopedGrant.sgGroupUid = PersonGroup.groupUid
               LEFT JOIN Person
                    ON Person.personGroupUid = PersonGroup.groupUid
         WHERE ScopedGrant.sgUid = :sgUid 
    """)
    abstract fun findByUidLiveWithName(sgUid: Long): DoorLiveData<ScopedGrantWithName?>

    companion object {

        const val SQL_FIND_BY_TABLE_AND_ENTITY = """
        SELECT ScopedGrant.*,
               CASE
               WHEN Person.firstNames IS NOT NULL THEN Person.firstNames
               ELSE PersonGroup.groupName 
               END AS name
          FROM ScopedGrant
               JOIN PersonGroup 
                    ON ScopedGrant.sgGroupUid = PersonGroup.groupUid
               LEFT JOIN Person
                         ON Person.personGroupUid = PersonGroup.groupUid
         WHERE ScopedGrant.sgTableId = :tableId
               AND ScopedGrant.sgEntityUid = :entityUid  
    """

    }

}