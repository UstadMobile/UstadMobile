package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Holiday

@DoorDao
@Repository
expect abstract class HolidayDao: BaseDao<Holiday>, OneToManyJoinDao<Holiday> {

    @Query("SELECT * FROM Holiday WHERE holHolidayCalendarUid = :holidayCalendarUid")
    abstract fun findByHolidayCalendaUid(holidayCalendarUid: Long): List<Holiday>

    @Query("SELECT * FROM Holiday WHERE holHolidayCalendarUid = :holidayCalendarUid")
    abstract suspend fun findByHolidayCalendaUidAsync(holidayCalendarUid: Long): List<Holiday>

    @Query("""
        UPDATE Holiday 
           SET holActive = :active, 
               holLct = :changeTime
         WHERE holUid = :holidayUid""")
    abstract fun updateActiveByUid(holidayUid: Long, active: Boolean, changeTime: Long)

    @Insert
    abstract suspend fun updateAsync(entity: Holiday)
}