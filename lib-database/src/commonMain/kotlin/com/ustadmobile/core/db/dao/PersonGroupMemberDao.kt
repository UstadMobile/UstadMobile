package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.PersonGroupMember

@Repository
@Dao
abstract class PersonGroupMemberDao : BaseDao<PersonGroupMember> {

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
