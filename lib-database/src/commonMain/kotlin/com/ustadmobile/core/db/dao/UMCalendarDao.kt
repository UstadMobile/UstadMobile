package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.UMCalendar
import com.ustadmobile.lib.db.entities.UMCalendarWithNumEntries

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class UMCalendarDao : BaseDao<UMCalendar> {

    @Insert
    abstract override fun insert(entity: UMCalendar): Long

    @Update
    abstract override fun update(entity: UMCalendar)

    @Query("SELECT * FROM UMCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1 AND " +
            " umCalendarCategory = " + UMCalendar.CATEGORY_HOLIDAY)
    abstract fun findAllHolidays(): DataSource.Factory<Int, UMCalendar>

    @Query("SELECT UMCalendar.* ," +
            " (SELECT COUNT(*) FROM DateRange " +
            "   WHERE dateRangeUMCalendarUid = UMCalendar.umCalendarUid " +
            "   AND dateRange.dateRangeActive = 1) AS numEntries " +
            " FROM UMCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1 AND " +
            " umCalendarCategory = " + UMCalendar.CATEGORY_HOLIDAY)
    abstract fun findAllHolidaysWithEntriesCount(): DataSource.Factory<Int, UMCalendarWithNumEntries>

    @Query("SELECT * FROM UMCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1")
    abstract fun findAllUMCalendars(): DataSource.Factory<Int, UMCalendar>

    @Query("SELECT * FROM UMCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1")
    abstract fun findAllUMCalendarsAsLiveDataList(): DoorLiveData<List<UMCalendar>>

    @Query("SELECT * FROM UMCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1 AND umCalendarCategory = "
            + UMCalendar.CATEGORY_HOLIDAY)
    abstract fun findAllHolidaysLiveData(): DoorLiveData<List<UMCalendar>>

    @Query("SELECT * FROM UMCalendar WHERE umCalendarUid = :uid AND CAST(umCalendarActive AS INTEGER) = 1")
    abstract fun findByUidLive(uid: Long): DoorLiveData<UMCalendar?>

    @Update
    abstract suspend fun updateAsync(entity: UMCalendar):Int

    @Query("SELECT * FROM UMCalendar WHERE umCalendarName = :name")
    abstract fun findByName(name: String): UMCalendar?

    @Query("SELECT * FROM UMCalendar WHERE umCalendarUid = :uid")
    abstract fun findByUid(uid: Long): UMCalendar?

    @Query("SELECT * FROM UMCalendar WHERE umCalendarUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): UMCalendar?

    @Query("UPDATE UMCalendar SET umCalendarActive = 0 WHERE umCalendarUid = :uid")
    abstract suspend fun inactivateCalendarAsync(uid: Long): Int

}
