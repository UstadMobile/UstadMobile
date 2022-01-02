package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonGroupMember
import com.ustadmobile.lib.db.entities.Role

@Repository
@Dao
abstract class PersonGroupMemberDao : BaseDao<PersonGroupMember> {

    @Query("""
     REPLACE INTO PersonGroupMemberReplicate(pgmPk, pgmVersionId, pgmDestination)
      SELECT PersonGroupMember.groupMemberUid AS pgmUid,
             PersonGroupMember.groupMemberLct AS pgmVersionId,
             :newNodeId AS pgmDestination
        FROM UserSession
             JOIN PersonGroupMember
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_PERSON_SELECT}
                    ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             JOIN PersonGroupMember PersonsWithPerm_GroupMember
                    ON PersonsWithPerm_GroupMember.groupMemberPersonUid = Person.personUid
       WHERE PersonGroupMember.groupMemberLct != COALESCE(
             (SELECT pgmVersionId
                FROM PersonGroupMemberReplicate
               WHERE pgmPk = PersonGroupMember.groupMemberUid
                 AND pgmDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(pgmPk, pgmDestination) DO UPDATE
             SET pgmProcessed = false, pgmVersionId = EXCLUDED.pgmVersionId
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PersonGroupMember::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO PersonGroupMemberReplicate(pgmPk, pgmVersionId, pgmDestination)
  SELECT PersonGroupMember.groupMemberUid AS pgmUid,
         PersonGroupMember.groupMemberLct AS pgmVersionId,
         UserSession.usClientNodeId AS pgmDestination
    FROM ChangeLog
         JOIN PersonGroupMember
             ON ChangeLog.chTableId = ${PersonGroupMember.TABLE_ID}
                AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
         JOIN UserSession
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
         SET pgmProcessed = false, pgmVersionId = EXCLUDED.pgmVersionId
      */               
     """)
    @ReplicationRunOnChange([PersonGroupMember::class])
    @ReplicationCheckPendingNotificationsFor([PersonGroupMember::class])
    abstract suspend fun replicateOnChange()


    @Query("SELECT * FROM PersonGroupMember WHERE groupMemberPersonUid = :personUid " +
            "AND PersonGroupMember.groupMemberActive")
    abstract suspend fun findAllGroupWherePersonIsIn(personUid: Long) : List<PersonGroupMember>

    @Query("""SELECT * FROM PersonGroupMember WHERE groupMemberGroupUid = :groupUid 
             AND groupMemberPersonUid = :personUid AND PersonGroupMember.groupMemberActive""" )
    abstract suspend fun checkPersonBelongsToGroup(groupUid: Long, personUid: Long): List<PersonGroupMember>

    /**
     * Updates an existing group membership to a new group
     */
    @Query("""UPDATE PersonGroupMember SET groupMemberGroupUid = :newGroup,
            groupMemberLastChangedBy = COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0)
            WHERE groupMemberPersonUid = :personUid AND groupMemberGroupUid = :oldGroup 
            AND PersonGroupMember.groupMemberActive""")
    abstract suspend fun moveGroupAsync(personUid: Long, newGroup: Long, oldGroup: Long): Int

    @Query("""UPDATE PersonGroupMember SET groupMemberActive = 0, 
        groupMemberLastChangedBy = COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) 
        WHERE groupMemberPersonUid = :personUid AND groupMemberGroupUid = :groupUid 
        AND PersonGroupMember.groupMemberActive""")
    abstract suspend fun setGroupMemberToInActive(personUid: Long, groupUid: Long)

}
