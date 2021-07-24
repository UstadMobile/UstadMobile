package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.HolidayCalendarDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries

class HolidayCalendarDaoJs: HolidayCalendarDao() {
    override fun findAllHolidaysWithEntriesCount(): DataSource.Factory<Int, HolidayCalendarWithNumEntries> {
        TODO("Not yet implemented")
    }

    override fun replaceList(list: List<HolidayCalendar>) {
        TODO("Not yet implemented")
    }

    override fun findAllHolidaysLiveData(): DoorLiveData<List<HolidayCalendar>> {
        TODO("Not yet implemented")
    }

    override fun findByUidLive(uid: Long): DoorLiveData<HolidayCalendar?> {
        TODO("Not yet implemented")
    }

    override suspend fun updateAsync(entity: HolidayCalendar): Int {
        TODO("Not yet implemented")
    }

    override suspend fun findByUid(uid: Long): HolidayCalendar? {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidAsync(uid: Long): HolidayCalendar? {
        TODO("Not yet implemented")
    }

    override fun insert(entity: HolidayCalendar): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: HolidayCalendar): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<HolidayCalendar>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<HolidayCalendar>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: HolidayCalendar) {
        TODO("Not yet implemented")
    }

    companion object {
        val ENTRIES = listOf(
            HolidayCalendar().apply {
                umCalendarUid = 1
                umCalendarName = "Sample calender 1"
            },

            HolidayCalendar().apply {
                umCalendarUid = 2
                umCalendarName = "Sample calender 2"
            },
            HolidayCalendar().apply {
                umCalendarUid = 3
                umCalendarName = "Sample calender 3"
            }
        )
    }
}