package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.CustomFieldValueOption

@UmDao(insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@Dao
@UmRepository
abstract class CustomFieldValueOptionDao : BaseDao<CustomFieldValueOption> {

    @Update
    abstract suspend fun updateAsync(entity: CustomFieldValueOption) : Int

    @Query("SELECT * FROM CustomFieldValueOption WHERE customFieldValueOptionUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : CustomFieldValueOption?
}
