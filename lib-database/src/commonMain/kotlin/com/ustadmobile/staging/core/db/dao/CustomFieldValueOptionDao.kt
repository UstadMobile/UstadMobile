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

    @Query("SELECT * FROM CustomFieldValueOption " +
            " WHERE customFieldValueOptionFieldUid = :customFieldUid " +
            " AND CAST(customFieldValueOptionActive AS INTEGER) = 1")
    abstract fun findAllOptionsForField(customFieldUid: Long): DataSource.Factory<Int, CustomFieldValueOption>

    @Query("SELECT * FROM CustomFieldValueOption " +
            " WHERE customFieldValueOptionFieldUid = :customFieldUid " +
            " AND CAST(customFieldValueOptionActive AS INTEGER) = 1")
    abstract suspend fun findAllOptionsForFieldAsync(customFieldUid: Long):List<CustomFieldValueOption>

    @Query("UPDATE CustomFieldValueOption SET customFieldValueOptionActive = 0 WHERE" + 
            " customFieldValueOptionUid = :uid")
    abstract suspend fun deleteOption(uid: Long): Int


    @Update
    abstract suspend fun updateAsync(entity: CustomFieldValueOption) : Int

    @Query("SELECT * FROM CustomFieldValueOption WHERE customFieldValueOptionUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : CustomFieldValueOption?
}
