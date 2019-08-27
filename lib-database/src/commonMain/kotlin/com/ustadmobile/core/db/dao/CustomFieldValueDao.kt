package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.CustomFieldValue

@UmDao(insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class CustomFieldValueDao : BaseDao<CustomFieldValue> {

    //TODO: Wrong , Check it:
    @Query("SELECT CustomFieldValue.* FROM CustomFieldValue " +
            " LEFT JOIN CustomField ON CustomField.customFieldUid = CustomFieldValue.customFieldValueFieldUid " +
            " WHERE customFieldValueEntityUid = :uid AND " +
            " CustomField.customFieldEntityType = :type LIMIT 1")
    abstract suspend fun findByEntityTypeAndUid(type: Int, uid: Long): CustomFieldValue?


    @Query("SELECT * FROM CustomFieldValue WHERE customFieldValueFieldUid = :fieldUid AND "
            + " customFieldValueEntityUid = :entityUid ")
    abstract suspend fun findValueByCustomFieldUidAndEntityUid(fieldUid: Long, entityUid: Long)
            : CustomFieldValue?

    @Query("SELECT * FROM CustomFieldValue WHERE customFieldValueFieldUid = :fieldUid AND "
            + " customFieldValueEntityUid = :entityUid ")
    abstract fun findValueByCustomFieldUidAndEntityUidSync(fieldUid: Long, entityUid: Long)
            : CustomFieldValue?


}
