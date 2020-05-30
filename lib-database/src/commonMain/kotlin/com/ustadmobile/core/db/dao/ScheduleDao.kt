package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.DateRange
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ScheduledCheck


@UmDao(inheritPermissionFrom = ClazzDao::class, 
        inheritPermissionForeignKey = "scheduleClazzUid", 
        inheritPermissionJoinedPrimaryKey = "clazzUid")
@UmRepository
@Dao
abstract class ScheduleDao : BaseDao<Schedule>, OneToManyJoinDao<Schedule> {

    @Insert
    abstract override fun insert(entity: Schedule): Long

    @Update
    abstract suspend fun updateAsync(entity: Schedule) : Int

    @Transaction
    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach { updateScheduleActivated(it, false) }
    }

    @Query("UPDATE Schedule SET scheduleActive = :active WHERE scheduleUid = :scheduleUid")
    abstract fun updateScheduleActivated(scheduleUid: Long, active: Boolean)

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract fun findByUid(uid: Long): Schedule?

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Schedule?

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1 ")
    abstract fun findAllSchedulesByClazzUid(clazzUid: Long): DataSource.Factory<Int, Schedule>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1")
    abstract fun findAllSchedulesByClazzUidAsList(clazzUid: Long): List<Schedule>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1 ")
    abstract suspend fun findAllSchedulesByClazzUidAsync(clazzUid: Long): List<Schedule>

}
