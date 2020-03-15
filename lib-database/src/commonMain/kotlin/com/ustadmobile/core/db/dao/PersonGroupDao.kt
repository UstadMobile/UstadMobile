package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.GroupWithMemberCount
import com.ustadmobile.lib.db.entities.PersonGroup

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class PersonGroupDao : BaseDao<PersonGroup> {

    @Query("SELECT *, 0 AS memberCount FROM PersonGroup")
    abstract fun findAllGroups(): DataSource.Factory<Int, GroupWithMemberCount>

    @Query("SELECT PersonGroup.*, " +
            " (SELECT COUNT(*) FROM PersonGroupMember " +
            "  WHERE groupMemberGroupUid = PersonGroup.groupUid AND" +
            "  CAST(groupMemberActive AS INTEGER) = 1) AS memberCount " +
            "FROM PersonGroup WHERE CAST(groupActive AS INTEGER) = 1 ORDER BY PersonGroup.groupName ASC")
    abstract fun findAllActiveGroups(): DataSource.Factory<Int, GroupWithMemberCount>

    @Query("SELECT PersonGroup.*, " +
            " (SELECT COUNT(*) FROM PersonGroupMember " +
            "  WHERE groupMemberGroupUid = PersonGroup.groupUid AND" +
            "  CAST(groupMemberActive AS INTEGER) = 1) AS memberCount " +
            "FROM PersonGroup WHERE CAST(groupActive AS INTEGER) = 1 AND groupPersonUid = 0 ORDER BY PersonGroup.groupName ASC")
    abstract fun findAllActiveGroupsWithoutIndividualGroup(): DataSource.Factory<Int, GroupWithMemberCount>

    @Query("SELECT *, 0 AS memberCount FROM PersonGroup WHERE CAST(groupActive AS INTEGER) = 1")
    abstract fun findAllActiveGroupsLive(): DoorLiveData<List<GroupWithMemberCount>>

    @Query("SELECT * FROM PersonGroup WHERE CAST(groupActive AS INTEGER) = 1")
    abstract fun findAllActivePersonGroupsLive(): DoorLiveData<List<PersonGroup>>

    @Query("SELECT * FROM PersonGroup WHERE CAST(groupActive AS INTEGER) = 1 AND groupPersonUid = 0")
    abstract fun findAllActiveGroupPersonGroupsLive(): DoorLiveData<List<PersonGroup>>

    @Query("SELECT PersonGroup.groupMasterCsn, PersonGroup.groupLocalCsn, " +
            " PersonGroup.groupLastChangedBy, " +
            " PersonGroup.groupActive, PersonGroup.groupPersonUid, PersonGroup.groupUid, " +
            " Person.firstNames||' '||Person.lastName as groupName " +
            " FROM PersonGroup LEFT JOIN PERSON ON Person.personUid = PersonGroup.groupPersonUid " +
            "  WHERE CAST(groupActive AS INTEGER) = 1 AND groupPersonUid != 0 " +
            "  AND CAST(Person.active AS INTEGER) = 1")
    abstract fun findAllActivePersonPersonGroupLive(): DoorLiveData<List<PersonGroup>>

    @Query("SELECT * FROM PersonGroup WHERE groupPersonUid = :personUid ")
    abstract suspend fun findPersonIndividualGroup(personUid: Long): PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupPersonUid = :personUid AND CAST(groupActive AS INTEGER) = 1 ")
    abstract fun findPersonIndividualGroupSync(personUid: Long): PersonGroup?

    @Query("UPDATE PersonGroup SET groupActive = 0 WHERE groupUid = :uid")
    abstract suspend fun inactivateGroupAsync(uid: Long) : Int

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract fun findByUid(uid: Long): PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : PersonGroup?

    @Query("SELECT * FROM PersonGroup WHERE groupUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<PersonGroup?>

    @Update
    abstract suspend fun updateAsync(entity: PersonGroup) : Int

}
