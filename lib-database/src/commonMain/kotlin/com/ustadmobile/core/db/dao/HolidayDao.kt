package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.Holiday

@Dao
@Repository
abstract class HolidayDao: BaseDao<Holiday>, OneToManyJoinDao<Holiday> {

    @Query("SELECT * FROM Holiday WHERE holHolidayCalendarUid = :holidayCalendarUid")
    abstract fun findByHolidayCalendaUid(holidayCalendarUid: Long): List<Holiday>

    @Query("SELECT * FROM Holiday WHERE holHolidayCalendarUid = :holidayCalendarUid")
    abstract suspend fun findByHolidayCalendaUidAsync(holidayCalendarUid: Long): List<Holiday>

    @Query("""UPDATE Holiday SET holActive = :active, 
        holLastModBy =  COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) 
        WHERE holUid = :holidayUid""")
    abstract suspend fun updateActiveByUidAsync(holidayUid: Long, active: Boolean)

    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateActiveByUidAsync(it, false)
        }
    }

    @Insert
    abstract suspend fun updateAsync(entity: Holiday)
}