package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Role


@UmRepository
@Dao
abstract class ClazzLogDao : BaseDao<ClazzLog> {

    @Insert
    abstract override fun insert(entity: ClazzLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replace(entity: ClazzLog): Long

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract fun findByUid(uid: Long): ClazzLog?

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): ClazzLog?

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<ClazzLog?>

    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE clazzLogClazzUid = :clazzUid
        AND clazzLog.clazzLogStatusFlag != :excludeStatus
        ORDER BY ClazzLog.logDate DESC""")
    abstract fun findByClazzUidAsFactory(clazzUid: Long, excludeStatus: Int): DataSource.Factory<Int, ClazzLog>


    //Used by the attendance recording screen to allow the user to go next/prev between days.
    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE clazzLogClazzUid = :clazzUid
        AND clazzLog.clazzLogStatusFlag != :excludeStatus
        ORDER BY ClazzLog.logDate ASC""")
    abstract fun findByClazzUidAsLiveData(clazzUid: Long, excludeStatus: Int): DoorLiveData<List<ClazzLog>>



    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE 
        ClazzLog.clazzLogClazzUid = :clazzUid 
        AND ClazzLog.logDate BETWEEN :fromTime AND :toTime
        AND (:excludeStatusFilter = 0 OR ((ClazzLog.clazzLogStatusFlag & :excludeStatusFilter) = 0))
        ORDER BY ClazzLog.logDate DESC
        LIMIT :limit
    """)
    abstract suspend fun findByClazzUidWithinTimeRange(clazzUid: Long, fromTime: Long, toTime: Long, excludeStatusFilter: Int, limit: Int): List<ClazzLog>

    suspend fun findByClazzUidWithinTimeRange(clazzUid: Long, fromTime: Long, toTime: Long) = findByClazzUidWithinTimeRange(clazzUid, fromTime, toTime, 0, Int.MAX_VALUE)

    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE 
        ClazzLog.clazzLogClazzUid = :clazzUid 
        AND ClazzLog.logDate BETWEEN :fromTime AND :toTime
        AND (:statusFilter = 0 OR ClazzLog.clazzLogStatusFlag = :statusFilter)
        ORDER BY ClazzLog.logDate
    """)
    abstract fun findByClazzUidWithinTimeRangeLive(clazzUid: Long, fromTime: Long, toTime: Long, statusFilter: Int): DoorLiveData<List<ClazzLog>>

    @Query("""
        SELECT EXISTS(SELECT ClazzLog.clazzLogUid FROM ClazzLog WHERE clazzLogClazzUid = :clazzUid 
        AND (:excludeStatusFilter = 0 OR ((ClazzLog.clazzLogStatusFlag & :excludeStatusFilter) = 0)))
    """)
    @QueryLiveTables(["ClazzLog"])
    abstract fun clazzHasScheduleLive(clazzUid: Long, excludeStatusFilter: Int): DoorLiveData<Boolean>


    @Query("""UPDATE ClazzLog 
        SET clazzLogStatusFlag = :newStatus,
        clazzLogLCB = (SELECT nodeClientId FROM SyncNode LIMIT 1)
        WHERE clazzLogUid = :clazzLogUid""")
    abstract fun updateStatusByClazzLogUid(clazzLogUid: Long, newStatus: Int)

}
