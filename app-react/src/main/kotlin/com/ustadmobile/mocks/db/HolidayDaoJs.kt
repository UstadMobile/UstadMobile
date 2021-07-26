package com.ustadmobile.mocks.db

import com.ustadmobile.core.db.dao.HolidayDao
import com.ustadmobile.lib.db.entities.Holiday

class HolidayDaoJs: HolidayDao() {

    override fun findByHolidayCalendaUid(holidayCalendarUid: Long): List<Holiday> {
       return ENTRIES.filter { it.holHolidayCalendarUid == holidayCalendarUid }
    }

    override suspend fun findByHolidayCalendaUidAsync(holidayCalendarUid: Long): List<Holiday> {
        console.log(holidayCalendarUid, "reached here")
        return ENTRIES.filter { it.holHolidayCalendarUid == holidayCalendarUid }
    }

    override fun updateActiveByUid(holidayUid: Long, active: Boolean) {}

    override suspend fun updateAsync(entity: Holiday) {}

    override fun insert(entity: Holiday): Long {return 1}

    override suspend fun insertAsync(entity: Holiday): Long { return 1}

    override fun insertList(entityList: List<Holiday>) {}

    override fun updateList(entityList: List<Holiday>) {}

    override fun update(entity: Holiday) {}

    override suspend fun insertListAsync(entityList: List<Holiday>) {}

    override suspend fun updateListAsync(entityList: List<Holiday>) {}

    companion object {
        val ENTRIES = listOf(
            Holiday().apply {
                holUid = 1
                holName = "Holiday 1"
                holStartTime = 1627299712000
                holEndTime = 1627899712000
                holHolidayCalendarUid = HolidayCalendarDaoJs.ENTRIES[0].umCalendarUid
            },
            Holiday().apply {
                holUid = 2
                holName = "Holiday 2"
                holStartTime = 1627299712000
                holEndTime = 1627899712000
                holHolidayCalendarUid = HolidayCalendarDaoJs.ENTRIES[0].umCalendarUid
            },
            Holiday().apply {
                holUid = 3
                holName = "Holiday 3"
                holStartTime = 1627299712000
                holEndTime = 1627899712000
                holHolidayCalendarUid = HolidayCalendarDaoJs.ENTRIES[1].umCalendarUid
            }
        )
    }
}