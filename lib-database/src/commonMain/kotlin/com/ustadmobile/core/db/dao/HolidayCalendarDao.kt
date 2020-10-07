package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class  HolidayCalendarDao : BaseDao<HolidayCalendar> {

    @Query("""SELECT HolidayCalendar.* ,
            (SELECT COUNT(*) FROM Holiday 
               WHERE holHolidayCalendarUid = HolidayCalendar.umCalendarUid 
               AND CAST(holActive AS INTEGER) = 1) AS numEntries 
             FROM HolidayCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1 AND 
             umCalendarCategory = ${HolidayCalendar.CATEGORY_HOLIDAY}""")
    abstract fun findAllHolidaysWithEntriesCount(): DataSource.Factory<Int, HolidayCalendarWithNumEntries>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(list: List<HolidayCalendar>)

    @Query("SELECT * FROM HolidayCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1 AND umCalendarCategory = "
            + HolidayCalendar.CATEGORY_HOLIDAY)
    abstract fun findAllHolidaysLiveData(): DoorLiveData<List<HolidayCalendar>>

    @Query("SELECT * FROM HolidayCalendar WHERE umCalendarUid = :uid AND CAST(umCalendarActive AS INTEGER) = 1")
    abstract fun findByUidLive(uid: Long): DoorLiveData<HolidayCalendar?>

    @Update
    abstract suspend fun updateAsync(entity: HolidayCalendar):Int

    @Query("SELECT * FROM HolidayCalendar WHERE umCalendarUid = :uid")
    abstract suspend fun findByUid(uid: Long): HolidayCalendar?

    @Query("SELECT * FROM HolidayCalendar WHERE umCalendarUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): HolidayCalendar?

}
