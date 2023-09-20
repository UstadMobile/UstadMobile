package com.ustadmobile.core.db.dao

import com.ustadmobile.door.paging.DataSourceFactory
import androidx.room.*
import com.ustadmobile.core.db.dao.EntityRoleCommon.FILTER_BY_PERSON_UID2
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.PostgresQuery
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.*

@Repository
@DoorDao
expect abstract class EntityRoleDao {

    @Query("""
        SELECT COALESCE((
               SELECT admin 
                 FROM Person 
                WHERE personUid = :accountPersonUid), 0)
            OR EXISTS(SELECT EntityRole.erUid FROM EntityRole 
               JOIN Role 
                    ON EntityRole.erRoleUid = Role.roleUid 
               JOIN PersonGroupMember 
                    ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
               AND (Role.rolePermissions & :permission) > 0) AS hasPermission""")
    @PostgresQuery("""
        SELECT COALESCE((
               SELECT admin 
                 FROM Person 
                WHERE personUid = :accountPersonUid), false)
            OR EXISTS(SELECT EntityRole.erUid FROM EntityRole 
               JOIN Role 
                    ON EntityRole.erRoleUid = Role.roleUid 
               JOIN PersonGroupMember 
                    ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
               AND (Role.rolePermissions & :permission) > 0) AS hasPermission
    """)
    @Deprecated("This should use ScopedGrant instead: see ScopedGrantDao.userHasSystemLevelPermissionAsFlow")
    abstract suspend fun userHasTableLevelPermission(accountPersonUid: Long, permission: Long) : Boolean


}
