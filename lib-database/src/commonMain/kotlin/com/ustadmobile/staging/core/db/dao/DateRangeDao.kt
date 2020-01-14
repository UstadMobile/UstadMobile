package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.DateRange

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class DateRangeDao : BaseDao<DateRange> {

    @Insert
    abstract override fun insert(entity: DateRange): Long

    @Update
    abstract override fun update(entity: DateRange)

    @Query("SELECT * FROM DateRange WHERE dateRangeUMCalendarUid = :calendarUid")
    abstract fun findAllDatesInCalendar(calendarUid: Long): DataSource.Factory<Int, DateRange>

    @Query("SELECT * FROM DateRange WHERE dateRangeUid = :uid")
    abstract fun findByUid(uid: Long): DateRange?

    @Query("SELECT * FROM DateRange WHERE dateRangeUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): DateRange?

    @Update
    abstract suspend fun updateAsync(entity: DateRange): Int

}
