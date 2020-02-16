package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.CustomFieldWrapper
import com.ustadmobile.lib.db.entities.PersonField

@UmDao(insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class PersonCustomFieldDao : BaseDao<PersonField> {

    @Insert
    abstract override fun insert(entity: PersonField): Long

    @Query("SELECT * FROM PersonField WHERE personCustomFieldUid = :uid")
    abstract fun findByUid(uid: Long): PersonField?

    @Query("SELECT * FROM PersonField WHERE personCustomFieldUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : PersonField?

    @Query("SELECT MAX(personCustomFieldUid) FROM PersonField")
    abstract fun findLatestUid(): Int

    @Query("SELECT * FROM PersonField ")
    abstract suspend fun findAllCustomFields() : List<PersonField>

    @Query("SELECT * FROM PersonField WHERE fieldName = :name")
    abstract suspend fun findByFieldNameAsync(name: String) : List<PersonField>

    @Query("SELECT * FROM PersonField WHERE fieldName = :fieldName")
    abstract suspend fun findByfieldName(fieldName: String) : PersonField?

    @Query("SELECT * FROM PersonField WHERE labelMessageId = :messageId")
    abstract suspend fun findByLabelMessageId(messageId: String) : PersonField?

    @Query("SELECT * FROM PersonField WHERE labelMessageId = :messageId")
    abstract fun findByLabelMessageIdSync(messageId: String): PersonField?

    @Query("SELECT PersonField.fieldName AS fieldName, '' AS fieldType, '' AS defaultValue " +
            "FROM PersonField " +
            "WHERE personCustomFieldUid > :minCustomFieldUid ")
    abstract fun findAllCustomFieldsProvider(minCustomFieldUid: Int): DataSource.Factory<Int, CustomFieldWrapper>

    @Update
    abstract suspend fun updateAsync(entity: PersonField): Int
}
