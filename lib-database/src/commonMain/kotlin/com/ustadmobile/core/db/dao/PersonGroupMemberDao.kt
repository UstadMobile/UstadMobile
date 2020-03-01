package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonGroupMember
import com.ustadmobile.lib.db.entities.PersonWithEnrollment

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class PersonGroupMemberDao : BaseDao<PersonGroupMember> {

    @Query("SELECT * FROM PersonGroupMember WHERE groupMemberPersonUid = :personUid")
    abstract suspend fun findAllGroupWherePersonIsIn(personUid: Long) : List<PersonGroupMember>

    @Query("SELECT * FROM PersonGroupMember WHERE groupMemberPersonUid = :personUid")
    abstract fun findAllGroupWherePersonIsInSync(personUid: Long): List<PersonGroupMember>

    @Query("SELECT * FROM PersonGroupMember WHERE groupMemberGroupUid = :groupUid " +
            " AND CAST(groupMemberActive AS INTEGER) = 1 ")
    abstract fun finAllMembersWithGroupId(groupUid: Long): DataSource.Factory<Int, PersonGroupMember>

    @Query("SELECT Person.*, Role.*, (0) AS clazzUid, " +
            "   '' AS clazzName, " +
            "  (0) AS attendancePercentage, " +
            "  (0) AS clazzMemberRole,  " +
            "  (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            "  PersonPicture.personPicturePersonUid = Person.personUid ORDER BY picTimestamp " +
            "  DESC LIMIT 1) AS personPictureUid, " +
            "  (0) AS enrolled from PersonGroupMember " +
            " LEFT JOIN Person ON PersonGroupMember.groupMemberPersonUid = Person.personUid " +
            " LEFT JOIN Role ON Role.roleUid = Person.personRoleUid " +
            " WHERE groupMemberGroupUid = :groupUid AND CAST(groupMemberActive AS INTEGER) = 1 ")
    abstract fun findAllPersonWithEnrollmentWithGroupUid(groupUid: Long): DataSource.Factory<Int, PersonWithEnrollment>

    @Query("Select Person.* from PersonGroupMember " +
            " LEFT JOIN Person on PersonGroupMember.groupMemberPersonUid = Person.personUid " +
            " WHERE PersonGroupMember.groupMemberGroupUid = :groupUid")
    abstract fun findPersonByGroupUid(groupUid: Long): List<Person>

    @Query("SELECT * FROM PersonGroupMember WHERE groupMemberGroupUid = :groupUid AND " + " groupMemberPersonUid = :personUid ")
    abstract suspend fun findMemberByGroupAndPersonAsync(groupUid: Long, personUid: Long)
            : PersonGroupMember?

    @Query("UPDATE PersonGroupMember SET groupMemberActive = 0 " +
            " , groupMemberLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) " +
            " WHERE groupMemberPersonUid = :personUid AND groupMemberGroupUid = :groupUid")
    abstract suspend fun inactivateMemberFromGroupAsync(personUid: Long, groupUid: Long) : Int

}
