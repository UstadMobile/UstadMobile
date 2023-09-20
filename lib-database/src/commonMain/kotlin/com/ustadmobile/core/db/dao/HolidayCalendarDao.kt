package com.ustadmobile.core.db.dao

import app.cash.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.lib.db.entities.UserSession

@Repository
@DoorDao
expect abstract class  HolidayCalendarDao : BaseDao<HolidayCalendar> {

    @Query("""SELECT HolidayCalendar.* ,
            (SELECT COUNT(*) FROM Holiday 
               WHERE holHolidayCalendarUid = HolidayCalendar.umCalendarUid 
               AND CAST(holActive AS INTEGER) = 1) AS numEntries 
             FROM HolidayCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1 AND 
             umCalendarCategory = ${HolidayCalendar.CATEGORY_HOLIDAY}""")
    abstract fun findAllHolidaysWithEntriesCount(): PagingSource<Int, HolidayCalendarWithNumEntries>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(list: List<HolidayCalendar>)

    @Query("SELECT * FROM HolidayCalendar WHERE CAST(umCalendarActive AS INTEGER) = 1 AND umCalendarCategory = "
            + HolidayCalendar.CATEGORY_HOLIDAY)
    abstract fun findAllHolidaysLiveData(): Flow<List<HolidayCalendar>>

    @Query("SELECT * FROM HolidayCalendar WHERE umCalendarUid = :uid AND CAST(umCalendarActive AS INTEGER) = 1")
    abstract fun findByUidLive(uid: Long): Flow<HolidayCalendar?>

    @Update
    abstract suspend fun updateAsync(entity: HolidayCalendar):Int

    @Query("SELECT * FROM HolidayCalendar WHERE umCalendarUid = :uid")
    abstract suspend fun findByUid(uid: Long): HolidayCalendar?

    @Query("SELECT * FROM HolidayCalendar WHERE umCalendarUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): HolidayCalendar?

}
