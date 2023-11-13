package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Repository
@DoorDao
expect abstract class PersonGroupMemberDao : BaseDao<PersonGroupMember> {

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
