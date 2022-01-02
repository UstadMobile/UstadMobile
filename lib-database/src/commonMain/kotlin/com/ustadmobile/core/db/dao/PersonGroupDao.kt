package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonGroupReplicate.Companion.PERSONGROUP_REPLICATE_NOT_ALREADY_UPDATE_SQL
import com.ustadmobile.lib.db.entities.PersonGroupReplicate.Companion.SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.lib.db.entities.UserSession.Companion.USER_SESSION_NOT_LOCAL_DEVICE_SQL

@Repository
@Dao
abstract class PersonGroupDao : BaseDao<PersonGroup> {

    @Query("""
     REPLACE INTO PersonGroupReplicate(pgPk, pgVersionId, pgDestination)
      SELECT $SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL,
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
             SET pgProcessed = false, pgVersionId = EXCLUDED.pgVersionId
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO PersonGroupReplicate(pgPk, pgVersionId, pgDestination)
  SELECT $SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL ,
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
     SET pgProcessed = false, pgVersionId = EXCLUDED.pgVersionId
  */               
    """)
    @ReplicationRunOnChange([PersonGroup::class])
    @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
    abstract suspend fun replicateOnChange()


    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract fun findByUid(uid: Long): PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<PersonGroup?>


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
