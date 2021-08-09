package com.ustadmobile.mocks.db

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.core.db.dao.HolidayCalendarDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.mocks.DoorLiveDataJs

class HolidayCalendarDaoJs: HolidayCalendarDao() {
    override fun findAllHolidaysWithEntriesCount(): DoorDataSourceFactory<Int, HolidayCalendarWithNumEntries> {
        return DataSourceFactoryJs(ENTRIES)
    }

    override fun replaceList(list: List<HolidayCalendar>) {}

    override fun findAllHolidaysLiveData(): DoorLiveData<List<HolidayCalendar>> {
        return DoorLiveDataJs(ENTRIES)
    }

    override fun findByUidLive(uid: Long): DoorLiveData<HolidayCalendar?> {
        return DoorLiveDataJs(ENTRIES.first { it.umCalendarUid == uid })
    }

    override suspend fun updateAsync(entity: HolidayCalendar): Int { return 1}

    override suspend fun findByUid(uid: Long): HolidayCalendar? {
        return ENTRIES.first { it.umCalendarUid == uid }
    }

    override suspend fun findByUidAsync(uid: Long): HolidayCalendar? {
       return  ENTRIES.first { it.umCalendarUid == uid }
    }

    override fun insert(entity: HolidayCalendar): Long { return 1}

    override suspend fun insertAsync(entity: HolidayCalendar): Long { return 1}

    override fun insertList(entityList: List<HolidayCalendar>) {}

    override fun updateList(entityList: List<HolidayCalendar>) {}

    override fun update(entity: HolidayCalendar) {}

    companion object {
        val ENTRIES = listOf(
            HolidayCalendarWithNumEntries().apply {
                umCalendarUid = 1
                umCalendarName = "Sample calender 1"
                numEntries = 2
            },

            HolidayCalendarWithNumEntries().apply {
                umCalendarUid = 2
                umCalendarName = "Sample calender 2"
                numEntries = 1
            },
            HolidayCalendarWithNumEntries().apply {
                umCalendarUid = 3
                umCalendarName = "Sample calender 3"
                numEntries = 0
            }
        )
    }
}