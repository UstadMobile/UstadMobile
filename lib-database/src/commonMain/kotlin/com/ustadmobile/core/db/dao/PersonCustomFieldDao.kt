package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.core.impl.UmCallback
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

    @Insert
    abstract fun insertAsync(entity: PersonField, resultObject: UmCallback<Long>)

    @Query("SELECT * FROM PersonField WHERE personCustomFieldUid = :uid")
    abstract fun findByUid(uid: Long): PersonField

    @Query("SELECT * FROM PersonField WHERE personCustomFieldUid = :uid")
    abstract fun findByUidAsync(uid: Long, resultObject: UmCallback<PersonField>)

    @Query("SELECT MAX(personCustomFieldUid) FROM PersonField")
    abstract fun findLatestUid(): Int

    @Query("SELECT * FROM PersonField WHERE personCustomFieldUid > :minCustomFieldUid")
    abstract fun findAllCustomFields(minCustomFieldUid: Int,
                                     resultObject: UmCallback<List<PersonField>>)

    @Query("SELECT * FROM PersonField WHERE fieldName = :name")
    abstract fun findByFieldNameAsync(name: String, resultList: UmCallback<List<PersonField>>)

    @Query("SELECT * FROM PersonField WHERE fieldName = :fieldName")
    abstract fun findByfieldName(fieldName: String, resultObject: UmCallback<PersonField>)

    @Query("SELECT * FROM PersonField WHERE labelMessageId = :messageId")
    abstract fun findByLabelMessageId(messageId: String, resultObject: UmCallback<PersonField>)

    @Query("SELECT * FROM PersonField WHERE labelMessageId = :messageId")
    abstract fun findByLabelMessageIdSync(messageId: String): PersonField

    @Query("SELECT PersonField.fieldName AS fieldName, '' AS fieldType, '' AS defaultValue " +
            "FROM PersonField " +
            "WHERE personCustomFieldUid > :minCustomFieldUid ")
    abstract fun findAllCustomFieldsProvider(minCustomFieldUid: Int): DataSource.Factory<Int, CustomFieldWrapper>
}
