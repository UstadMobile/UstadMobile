package com.ustadmobile.mocks.db

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.core.db.dao.ScheduleDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_MONDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_SUNDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.DAY_WEDNESDAY
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_APRIL
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_JANUARY
import com.ustadmobile.lib.db.entities.Schedule.Companion.MONTH_JULY
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_DAILY
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_MONTHLY
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_ONCE
import com.ustadmobile.lib.db.entities.Schedule.Companion.SCHEDULE_FREQUENCY_WEEKLY

class ScheduleDaoJs: ScheduleDao() {
    override fun insert(entity: Schedule): Long {
        TODO("Not yet implemented")
    }

    override suspend fun updateAsync(entity: Schedule): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updateScheduleActivated(scheduleUid: Long, active: Boolean) {
        TODO("Not yet implemented")
    }

    override fun findByUid(uid: Long): Schedule? {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidAsync(uid: Long): Schedule? {
        TODO("Not yet implemented")
    }

    override fun findAllSchedulesByClazzUid(clazzUid: Long): DoorDataSourceFactory<Int, Schedule> {
        return DataSourceFactoryJs(ENTRIES)
    }

    override fun findAllSchedulesByClazzUidAsList(clazzUid: Long): List<Schedule> {
        TODO("Not yet implemented")
    }

    override fun findAllSchedulesByClazzUidAsLiveList(clazzUid: Long): DoorLiveData<List<Schedule>> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSchedulesByClazzUidAsync(clazzUid: Long): List<Schedule> {
        return ENTRIES.filter { it.scheduleClazzUid == clazzUid }
    }

    override suspend fun insertAsync(entity: Schedule): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<Schedule>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<Schedule>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: Schedule) {
        TODO("Not yet implemented")
    }

    override suspend fun insertListAsync(entityList: List<Schedule>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateListAsync(entityList: List<Schedule>) {
        TODO("Not yet implemented")
    }

    companion object {
        val ENTRIES = listOf(
            Schedule().apply {
                scheduleUid = 1
                sceduleStartTime = 14 * 60 * 60 * 1000
                scheduleEndTime = (14.5 * 60 * 60 * 1000).toLong()
                scheduleFrequency = SCHEDULE_FREQUENCY_DAILY
                scheduleMonth = MONTH_JULY
                scheduleDay = DAY_WEDNESDAY
                scheduleActive = true
            },
            Schedule().apply {
                scheduleUid = 2
                sceduleStartTime = 16 * 60 * 60 * 1000
                scheduleEndTime = (17 * 60 * 60 * 1000).toLong()
                scheduleFrequency = SCHEDULE_FREQUENCY_WEEKLY
                scheduleMonth = MONTH_APRIL
                scheduleDay = DAY_SUNDAY
                scheduleActive = true
            },
            Schedule().apply {
                scheduleUid = 1
                sceduleStartTime = 14 * 60 * 60 * 1000
                scheduleEndTime = (14.5 * 60 * 60 * 1000).toLong()
                scheduleFrequency = SCHEDULE_FREQUENCY_ONCE
                scheduleMonth = MONTH_JANUARY
                scheduleDay = DAY_MONDAY
                scheduleActive = true
            }
        )
    }
}