package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.RoleDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Role

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
abstract class RoleDao : BaseDao<Role> {

    @Query("SELECT * FROM Role WHERE roleName=:roleName")
    abstract fun findByName(roleName: String, callback: UmCallback<Role>)

    @Query("SELECT * FROM Role WHERE roleName = :roleName")
    abstract fun findByNameSync(roleName: String): Role

    @Query("SELECT * FROM Role WHERE roleActive = 1")
    abstract fun findAllActiveRoles(): DataSource.Factory<Int, Role>

    @Query("SELECT * FROM Role WHERE roleActive = 1")
    abstract fun findAllActiveRolesLive(): DoorLiveData<List<Role>>

    @Query("UPDATE Role SET roleActive = 0 WHERE roleUid = :uid")
    abstract fun inactiveRole(uid: Long)

    @Query("UPDATE Role SET roleActive = 0 WHERE roleUid = :uid")
    abstract fun inactiveRoleAsync(uid: Long, resultObject: UmCallback<Int>)

    @Query("SELECT * FROM Role WHERE roleUid = :uid")
    abstract fun findByUidAsync(uid: Long, resultObject: UmCallback<Role>)

    @Query("SELECT * FROM Role WHERE roleUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Role>

    @Update
    abstract fun updateAsync(entitiy: Role, resultObject: UmCallback<Int>)

    companion object {

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"
    }
}
