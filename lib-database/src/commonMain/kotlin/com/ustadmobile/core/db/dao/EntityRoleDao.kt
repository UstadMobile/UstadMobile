package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.RoleDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.EntityRole

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)", 
        updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class EntityRoleDao : BaseDao<EntityRole> {

    @Query("SELECT * FROM EntityRole WHERE erUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : EntityRole?

    @Query("SELECT * FROM EntityRole WHERE erUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<EntityRole?>

    @Update
    abstract suspend fun updateAsync(entity: EntityRole) :Int


}
