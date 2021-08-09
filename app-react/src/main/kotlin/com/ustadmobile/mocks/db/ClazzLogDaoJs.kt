package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.ClazzLogDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.mocks.DoorLiveDataJs

class ClazzLogDaoJs: ClazzLogDao() {
    override fun replace(entity: ClazzLog): Long {
        TODO("Not yet implemented")
    }

    override fun findByUid(uid: Long): ClazzLog? {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidAsync(uid: Long): ClazzLog? {
        TODO("Not yet implemented")
    }

    override fun findByUidLive(uid: Long): DoorLiveData<ClazzLog?> {
        TODO("Not yet implemented")
    }

    override fun findByClazzUidAsFactory(
        clazzUid: Long,
        excludeStatus: Int
    ): DataSource.Factory<Int, ClazzLog> {
        return DataSourceFactoryJs(ENTRIES)
    }

    override suspend fun findByClazzUidAsync(clazzUid: Long, excludeStatus: Int): List<ClazzLog> {
        TODO("Not yet implemented")
    }

    override suspend fun findByClazzUidWithinTimeRangeAsync(
        clazzUid: Long,
        fromTime: Long,
        toTime: Long,
        excludeStatusFilter: Int,
        limit: Int
    ): List<ClazzLog> {
        return ENTRIES
    }

    override fun findByClazzUidWithinTimeRange(
        clazzUid: Long,
        fromTime: Long,
        toTime: Long,
        excludeStatusFilter: Int,
        limit: Int
    ): List<ClazzLog> {
        return ENTRIES
    }

    override fun findByClazzUidWithinTimeRangeLive(
        clazzUid: Long,
        fromTime: Long,
        toTime: Long,
        statusFilter: Int
    ): DoorLiveData<List<ClazzLog>> {
        return DoorLiveDataJs(ENTRIES.map {
            it.clazzLogStatusFlag = statusFilter
            it
        })
    }

    override fun clazzHasScheduleLive(
        clazzUid: Long,
        excludeStatusFilter: Int
    ): DoorLiveData<Boolean> {
        return DoorLiveDataJs(true)
    }

    override fun updateStatusByClazzLogUid(clazzLogUid: Long, newStatus: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAsync(clazzLog: ClazzLog) {
        TODO("Not yet implemented")
    }

    override fun insert(entity: ClazzLog): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: ClazzLog): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<ClazzLog>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<ClazzLog>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: ClazzLog) {
        TODO("Not yet implemented")
    }

    companion object {
        val ENTRIES = listOf(
            ClazzLog().apply {
                clazzLogUid = 1
                clazzLogClazzUid = ClazzDaoJs.ENTRIES.first().clazzUid
                logDate = 1627924325000L
                timeRecorded = 1627819925000
                clazzLogDone = true
                clazzLogCancelled = false
                clazzLogNumPresent = 70
                clazzLogNumAbsent = 12
                clazzLogNumPartial = 4
                clazzLogScheduleUid = ScheduleDaoJs.ENTRIES.first().scheduleUid
            },
            ClazzLog().apply {
                clazzLogUid = 2
                clazzLogClazzUid = ClazzDaoJs.ENTRIES.first().clazzUid
                logDate = 1627819925000
                timeRecorded = 1627819925000
                clazzLogDone = false
                clazzLogCancelled = false
                clazzLogNumPresent = 55
                clazzLogNumAbsent = 6
                clazzLogNumPartial = 2
                clazzLogScheduleUid = ScheduleDaoJs.ENTRIES.first().scheduleUid
            },
            ClazzLog().apply {
                clazzLogUid = 3
                clazzLogClazzUid = ClazzDaoJs.ENTRIES.first().clazzUid
                logDate = 1617279125000
                timeRecorded = 1627819925000
                clazzLogDone = false
                clazzLogCancelled = false
                clazzLogNumPresent = 30
                clazzLogNumAbsent = 1
                clazzLogNumPartial = 6
                clazzLogScheduleUid = ScheduleDaoJs.ENTRIES.first().scheduleUid
            },
            ClazzLog().apply {
                clazzLogUid = 4
                clazzLogClazzUid = ClazzDaoJs.ENTRIES.first().clazzUid
                logDate = 1619871125000
                timeRecorded = 1627819925000
                clazzLogDone = false
                clazzLogCancelled = false
                clazzLogNumPresent = 57
                clazzLogNumAbsent = 5
                clazzLogNumPartial = 1
                clazzLogScheduleUid = ScheduleDaoJs.ENTRIES.first().scheduleUid
                clazzLogStatusFlag = ClazzLog.STATUS_INACTIVE
            }
        )
    }
}