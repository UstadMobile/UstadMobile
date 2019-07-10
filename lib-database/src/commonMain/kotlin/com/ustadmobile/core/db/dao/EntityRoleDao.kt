package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.core.db.dao.RoleDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.EntityRole

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)",
        updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@Dao
@UmRepository
abstract class EntityRoleDao : BaseDao<EntityRole> {

    @Query("SELECT (SELECT admin FROM Person WHERE personUid = :accountPersonUid) " +
            "OR EXISTS(SELECT EntityRole.erUid FROM EntityRole " +
            " JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            " JOIN PersonGroupMember ON EntityRole.erGroupUid = " +
            "PersonGroupMember.groupMemberGroupUid WHERE " +
            " PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
            " AND EntityRole.erTableId = :tableId " +
            " AND (Role.rolePermissions & :permission) > 0) AS hasPermission")
    abstract suspend fun userHasTableLevelPermissionAsync(accountPersonUid: Long, tableId: Int,
                                                          permission: Long): Boolean

}
