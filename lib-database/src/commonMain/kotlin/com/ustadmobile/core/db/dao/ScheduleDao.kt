package com.ustadmobile.core.db.dao

import app.cash.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Schedule


@Repository
@DoorDao
expect abstract class ScheduleDao : BaseDao<Schedule> {

    @Insert
    abstract override fun insert(entity: Schedule): Long

    @Update
    abstract suspend fun updateAsync(entity: Schedule) : Int


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entities: List<Schedule>)


    @Query("""
        UPDATE Schedule 
           SET scheduleActive = :active,
               scheduleLastChangedTime = :changeTime
         WHERE scheduleUid = :scheduleUid
            """)
    abstract suspend fun updateScheduleActivated(
        scheduleUid: Long,
        active: Boolean,
        changeTime: Long
    )

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract fun findByUid(uid: Long): Schedule?

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Schedule?

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1 ")
    abstract fun findAllSchedulesByClazzUid(clazzUid: Long): PagingSource<Int, Schedule>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1")
    abstract fun findAllSchedulesByClazzUidAsList(clazzUid: Long): List<Schedule>

    //Used for testing ClazzEdit
    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1")
    abstract fun findAllSchedulesByClazzUidAsLiveList(clazzUid: Long): Flow<List<Schedule>>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1 ")
    abstract suspend fun findAllSchedulesByClazzUidAsync(clazzUid: Long): List<Schedule>

}
