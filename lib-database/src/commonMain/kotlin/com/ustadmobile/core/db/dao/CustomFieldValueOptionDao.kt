package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.CustomFieldValueOption

@UmDao(insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
abstract class CustomFieldValueOptionDao : BaseDao<CustomFieldValueOption> {

    @Query("SELECT * FROM CustomFieldValueOption " +
            " WHERE customFieldValueOptionFieldUid = :customFieldUid " +
            " AND customFieldValueOptionActive = 1")
    abstract fun findAllOptionsForField(customFieldUid: Long): DataSource.Factory<Int, CustomFieldValueOption>

    @Query("SELECT * FROM CustomFieldValueOption " +
            " WHERE customFieldValueOptionFieldUid = :customFieldUid " +
            " AND customFieldValueOptionActive = 1")
    abstract fun findAllOptionsForFieldAsync(customFieldUid: Long, optionsCallback: UmCallback<List<CustomFieldValueOption>>)

    @Query("UPDATE CustomFieldValueOption SET customFieldValueOptionActive = 0 WHERE" + 
            " customFieldValueOptionUid = :uid")
    abstract fun deleteOption(uid: Long, resultObjecT: UmCallback<Int>)


    @Update
    abstract fun updateAsync(entity: CustomFieldValueOption, resultObject: UmCallback<Int>)

    @Query("SELECT * FROM CustomFieldValueOption WHERE customFieldValueOptionUid = :uid")
    abstract fun findByUidAsync(uid: Long, resultObject: UmCallback<CustomFieldValueOption>)
}
