package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzLog


@Repository
@Dao
abstract class ClazzLogDao : BaseDao<ClazzLog> {

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
    abstract fun findByClazzUidAsFactory(clazzUid: Long, excludeStatus: Int): DoorDataSourceFactory<Int, ClazzLog>


    //Used by the attendance recording screen to allow the user to go next/prev between days.
    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE clazzLogClazzUid = :clazzUid
        AND clazzLog.clazzLogStatusFlag != :excludeStatus
        ORDER BY ClazzLog.logDate ASC""")
    abstract suspend fun findByClazzUidAsync(clazzUid: Long, excludeStatus: Int): List<ClazzLog>


    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE 
        ClazzLog.clazzLogClazzUid = :clazzUid 
        AND ClazzLog.logDate BETWEEN :fromTime AND :toTime
        AND (:excludeStatusFilter = 0 OR ((ClazzLog.clazzLogStatusFlag & :excludeStatusFilter) = 0))
        ORDER BY ClazzLog.logDate DESC
        LIMIT :limit
    """)
    abstract suspend fun findByClazzUidWithinTimeRangeAsync(clazzUid: Long, fromTime: Long, toTime: Long, excludeStatusFilter: Int, limit: Int): List<ClazzLog>


    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE 
        ClazzLog.clazzLogClazzUid = :clazzUid 
        AND ClazzLog.logDate BETWEEN :fromTime AND :toTime
        AND (:excludeStatusFilter = 0 OR ((ClazzLog.clazzLogStatusFlag & :excludeStatusFilter) = 0))
        ORDER BY ClazzLog.logDate DESC
        LIMIT :limit
    """)
    abstract fun findByClazzUidWithinTimeRange(clazzUid: Long, fromTime: Long, toTime: Long, excludeStatusFilter: Int = 0, limit: Int = Int.MAX_VALUE): List<ClazzLog>


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
        clazzLogLCB =  COALESCE((SELECT nodeClientId FROM SyncNode LIMIT 1), 0)
        WHERE clazzLogUid = :clazzLogUid""")
    abstract fun updateStatusByClazzLogUid(clazzLogUid: Long, newStatus: Int)

    @Update
    abstract suspend fun updateAsync(clazzLog: ClazzLog)

}
