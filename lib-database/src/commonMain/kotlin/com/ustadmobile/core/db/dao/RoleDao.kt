package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.RoleDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Role

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class RoleDao : BaseDao<Role> {

    @Query("SELECT * FROM Role WHERE roleName=:roleName")
    abstract suspend fun findByName(roleName: String): Role?

    @Query("SELECT * FROM Role WHERE roleName = :roleName")
    abstract fun findByNameSync(roleName: String): Role?

    @Query("SELECT * FROM Role WHERE CAST(roleActive AS INTEGER) = 1")
    abstract fun findAllActiveRoles(): DataSource.Factory<Int, Role>

    @Query("SELECT * FROM Role WHERE CAST(roleActive AS INTEGER) = 1")
    abstract fun findAllActiveRolesLive(): DoorLiveData<List<Role>>

    //TODO : Replace with boolean argument
    @Query("UPDATE Role SET roleActive = 0 WHERE roleUid = :uid")
    abstract fun inactiveRole(uid: Long)

    //TODO: Replace with boolean argument
    @Query("UPDATE Role SET roleActive = 0 WHERE roleUid = :uid")
    abstract suspend fun inactiveRoleAsync(uid: Long) :Int

    @Query("SELECT * FROM Role WHERE roleUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): Role?

    @Query("SELECT * FROM Role WHERE roleUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Role?>

    @Update
    abstract suspend fun updateAsync(entitiy: Role):Int

    companion object {

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"
    }
}
