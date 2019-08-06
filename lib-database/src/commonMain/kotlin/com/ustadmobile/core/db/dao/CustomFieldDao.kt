package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.database.annotation.UmUpdate
import com.ustadmobile.lib.db.entities.CustomField

@UmDao(insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class CustomFieldDao : BaseDao<CustomField> {

    @Query("SELECT * FROM CustomField WHERE customFieldUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<CustomField>

    @Query("SELECT * FROM CustomField WHERE customFieldUid = :uid")
    abstract fun findByUidAsync(uid: Long, resultObject: UmCallback<CustomField>)

    @UmUpdate
    abstract fun updateAsync(entity: CustomField, resultObjcet: UmCallback<Int>)

    @Query("SELECT * FROM CustomField WHERE customFieldEntityType = :tableId AND "
            + " customFieldActive = 1")
    abstract fun findAllCustomFieldsProviderForEntity(tableId: Int): DataSource.Factory<Int, CustomField>

    @Query("SELECT * FROM CustomField WHERE customFieldEntityType = :tableId AND "
            + " customFieldActive = 1")
    abstract fun findAllCustomFieldsProviderForEntityAsync(tableId: Int,
                                               listResultCallback: UmCallback<List<CustomField>>)

    @Query("UPDATE CustomField SET customFieldActive = 0 WHERE customFieldUid = :customFieldUid")
    abstract fun deleteCustomField(customFieldUid: Long, resultCallback: UmCallback<Int>)

    @Query("SELECT * FROM CustomField WHERE customFieldName = :fieldName COLLATE NOCASE AND "
            + "customFieldEntityType = :tableId AND customFieldActive = 1 ")
    abstract fun findByFieldNameAndEntityTypeAsync(fieldName: String, tableId: Int,
                                               listResultCallback: UmCallback<List<CustomField>>)

    //For debugging
    @Query("SELECT * FROM CustomField")
    abstract fun findAll(): List<CustomField>
}
