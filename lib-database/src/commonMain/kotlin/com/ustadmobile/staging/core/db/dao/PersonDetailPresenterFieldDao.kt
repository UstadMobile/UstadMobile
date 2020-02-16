package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM PersonDetailPresenterField WHERE fieldUid = :uid")
    abstract suspend fun findAllByFieldUid(uid: Long) : List<PersonDetailPresenterField>

    @Query("SELECT * FROM PersonDetailPresenterField WHERE personDetailPresenterFieldUid = :uid")
    abstract fun findByUid(uid: Long): PersonDetailPresenterField?

    @Query("SELECT * FROM PersonDetailPresenterField WHERE CAST(viewModeVisible AS INTEGER) = 1 ORDER BY fieldIndex")
    abstract fun findAllPersonDetailPresenterFieldsViewModeLive() : DoorLiveData<List<PersonDetailPresenterField>>

    @Query("SELECT * FROM PersonDetailPresenterField WHERE CAST(editModeVisible AS INTEGER) = 1 ORDER BY fieldIndex")
    abstract fun findAllPersonDetailPresenterFieldsEditModeLive() :
            DoorLiveData<List<PersonDetailPresenterField>>

    @Query("SELECT * FROM PersonDetailPresenterField WHERE fieldIndex = :id")
    abstract suspend fun findAllByFieldIndex(id: Int) : List<PersonDetailPresenterField>

    @Query("""
        SELECT * FROM PersonDetailPresenterField WHERE fieldUid = :fieldUid AND headerMessageId = 0
    """)
    abstract suspend fun findByFieldUidAsync(fieldUid: Long): PersonDetailPresenterField?

    @Query("""
        SELECT * FROM PersonDetailPresenterField WHERE headerMessageId = :headerUid AND fieldUid = 0
    """)
    abstract suspend fun findByHeaderUidAsync(headerUid: Long): PersonDetailPresenterField?

    @Query("""
        UPDATE PersonDetailPresenterField SET viewModeVisible = false, editModeVisible = false, 
        isReadyOnly = false WHERE fieldUid NOT IN (:uids) AND fieldUid != 0
    """)
    abstract suspend fun disableAllDetailFieldsExcept(uids: List<Long>): Int

    @Query("""
        UPDATE PersonDetailPresenterField SET viewModeVisible = false, editModeVisible = false, 
        isReadyOnly = false WHERE headerMessageId NOT IN (:uids) AND headerMessageId != 0
    """)
    abstract suspend fun disableAllDetailFieldsExceptHeader(uids: List<Long>): Int

    @Update
    abstract suspend fun updateAsync(entity: PersonDetailPresenterField): Int
}
