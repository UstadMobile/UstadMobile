package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.db.entities.PersonCustomField

@UmDao(insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@Dao
abstract class PersonCustomFieldDao : BaseDao<PersonCustomField> {

    @Insert
    abstract override fun insert(entity: PersonCustomField): Long

    @Insert
    abstract override suspend fun insertAsync(entity: PersonCustomField): Long

    @Query("SELECT * FROM PersonCustomField WHERE personCustomFieldUid = :uid")
    abstract override fun findByUid(uid: Long): PersonCustomField?
}
