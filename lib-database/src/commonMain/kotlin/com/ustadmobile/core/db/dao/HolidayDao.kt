package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.db.entities.Holiday

@Dao
abstract class HolidayDao: BaseDao<Holiday>, OneToManyJoinDao<Holiday> {

    @Query("SELECT * FROM Holiday WHERE holHolidayCalendarUid = :holidayCalendarUid")
    abstract fun findByHolidayCalendaUid(holidayCalendarUid: Long): List<Holiday>

    @Query("SELECT * FROM Holiday WHERE holHolidayCalendarUid = :holidayCalendarUid")
    abstract suspend fun findByHolidayCalendaUidAsync(holidayCalendarUid: Long): List<Holiday>

    @Query("""UPDATE Holiday SET holActive = :active, 
        holLastModBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
        WHERE holUid = :holidayUid""")
    abstract fun updateActiveByUid(holidayUid: Long, active: Boolean)

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach { updateActiveByUid(it, false) }
    }

    @Insert
    abstract suspend fun updateAsync(entity: Holiday)
}