package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
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

    @Query("SELECT * FROM PersonDetailPresenterField WHERE personDetailPresenterFieldUid = :uid")
    abstract fun findByUid(uid: Long): PersonDetailPresenterField?

    @Query("SELECT * FROM PersonDetailPresenterField WHERE CAST(viewModeVisible AS INTEGER) = 1 ORDER BY fieldIndex")
    abstract fun findAllPersonDetailPresenterFieldsViewModeLive() : DoorLiveData<List<PersonDetailPresenterField>>

    @Query("SELECT * FROM PersonDetailPresenterField WHERE CAST(editModeVisible AS INTEGER) = 1 ORDER BY fieldIndex")
    abstract fun findAllPersonDetailPresenterFieldsEditModeLive() :
            DoorLiveData<List<PersonDetailPresenterField>>

    @Query("SELECT * FROM PersonDetailPresenterField WHERE fieldIndex = :id")
    abstract suspend fun findAllByFieldIndex(id: Int) : List<PersonDetailPresenterField>
}
