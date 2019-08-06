package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class PersonDetailPresenterFieldDao : BaseDao<PersonDetailPresenterField> {

    @Insert
    abstract override fun insert(entity: PersonDetailPresenterField): Long

    @Insert
    abstract fun insertAsync(entity: PersonDetailPresenterField, result: UmCallback<Long>)

    @Query("SELECT * FROM PersonDetailPresenterField WHERE personDetailPresenterFieldUid = :uid")
    abstract fun findByUid(uid: Long): PersonDetailPresenterField

    @Query("SELECT * FROM PersonDetailPresenterField ORDER BY fieldIndex")
    abstract fun findAllPersonDetailPresenterFields(
            callback: UmCallback<List<PersonDetailPresenterField>>)

    @Query("SELECT * FROM PersonDetailPresenterField WHERE viewModeVisible = 1 ORDER BY fieldIndex")
    abstract fun findAllPersonDetailPresenterFieldsViewMode(
            callback: UmCallback<List<PersonDetailPresenterField>>)

    @Query("SELECT * FROM PersonDetailPresenterField WHERE editModeVisible = 1 ORDER BY fieldIndex")
    abstract fun findAllPersonDetailPresenterFieldsEditMode(
            callback: UmCallback<List<PersonDetailPresenterField>>)

    @Query("SELECT * FROM PersonDetailPresenterField WHERE fieldUid = :uid")
    abstract fun findAllByFieldUid(uid: Long, resultList: UmCallback<List<PersonDetailPresenterField>>)

    @Query("SELECT * FROM PersonDetailPresenterField WHERE labelMessageId = :id")
    abstract fun findAllByLabelMessageId(id: Int, resultList: UmCallback<List<PersonDetailPresenterField>>)

    @Query("SELECT * FROM PersonDetailPresenterField WHERE fieldIndex = :id")
    abstract fun findAllByFieldIndex(id: Int, resultList: UmCallback<List<PersonDetailPresenterField>>)
}
