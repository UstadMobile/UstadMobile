package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.Schedule


@Repository
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

    @Query("""UPDATE Schedule SET scheduleActive = :active,
            scheduleLastChangedBy = COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0) 
            WHERE scheduleUid = :scheduleUid""")
    abstract suspend fun updateScheduleActivated(scheduleUid: Long, active: Boolean)

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract fun findByUid(uid: Long): Schedule?

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Schedule?

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1 ")
    abstract fun findAllSchedulesByClazzUid(clazzUid: Long): DoorDataSourceFactory<Int, Schedule>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1")
    abstract fun findAllSchedulesByClazzUidAsList(clazzUid: Long): List<Schedule>

    //Used for testing ClazzEdit
    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1")
    abstract fun findAllSchedulesByClazzUidAsLiveList(clazzUid: Long): DoorLiveData<List<Schedule>>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1 ")
    abstract suspend fun findAllSchedulesByClazzUidAsync(clazzUid: Long): List<Schedule>

}
