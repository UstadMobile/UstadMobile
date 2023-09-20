package com.ustadmobile.core.db.dao

import app.cash.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Role


@Repository
@DoorDao
expect abstract class ClazzLogDao : BaseDao<ClazzLog> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replace(entity: ClazzLog): Long

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract fun findByUid(uid: Long): ClazzLog?

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): ClazzLog?

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract fun findByUidLive(uid: Long): Flow<ClazzLog?>

    @Query("""
        SELECT ClazzLog.* 
          FROM ClazzLog 
         WHERE clazzLogClazzUid = :clazzUid
           AND clazzLog.clazzLogStatusFlag != :excludeStatus
      ORDER BY ClazzLog.logDate DESC
    """)
    abstract fun findByClazzUidAsFactory(
        clazzUid: Long,
        excludeStatus: Int
    ): PagingSource<Int, ClazzLog>


    //Used by the attendance recording screen to allow the user to go next/prev between days.
    @Query("""
        SELECT ClazzLog.* 
          FROM ClazzLog 
         WHERE ClazzLog.clazzLogClazzUid = :clazzUid
           AND clazzLog.clazzLogStatusFlag != :excludeStatus
      ORDER BY ClazzLog.logDate ASC
    """)
    abstract suspend fun findByClazzUidAsync(
        clazzUid: Long,
        excludeStatus: Int
    ): List<ClazzLog>

    @Query("""
        SELECT ClazzLog.* 
          FROM ClazzLog 
         WHERE ClazzLog.clazzLogClazzUid = 
               (SELECT ClazzLogInner.clazzLogClazzUid
                  FROM ClazzLog ClazzLogInner
                 WHERE ClazzLogInner.clazzLogUid = :clazzLogUid)
           AND clazzLog.clazzLogStatusFlag != :excludeStatus
      ORDER BY ClazzLog.logDate ASC
    """)
    abstract suspend fun findAllForClazzByClazzLogUid(
        clazzLogUid: Long,
        excludeStatus: Int
    ): List<ClazzLog>

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
    abstract fun findByClazzUidWithinTimeRangeLive(clazzUid: Long, fromTime: Long, toTime: Long, statusFilter: Int): Flow<List<ClazzLog>>

    @Query("""
        SELECT EXISTS
               (SELECT ClazzLog.clazzLogUid 
                  FROM ClazzLog 
                 WHERE clazzLogClazzUid = :clazzUid 
                 AND (:excludeStatusFilter = 0 
                      OR ((ClazzLog.clazzLogStatusFlag & :excludeStatusFilter) = 0))
               )
    """)
    @QueryLiveTables(["ClazzLog"])
    abstract fun clazzHasScheduleLive(clazzUid: Long, excludeStatusFilter: Int): Flow<Boolean>


    @Query("""UPDATE ClazzLog 
        SET clazzLogStatusFlag = :newStatus,
        clazzLogLastChangedTime = :timeChanged
        WHERE clazzLogUid = :clazzLogUid""")
    abstract fun updateStatusByClazzLogUid(clazzLogUid: Long, newStatus: Int, timeChanged: Long)

    @Update
    abstract suspend fun updateAsync(clazzLog: ClazzLog)

    @Query("""
        SELECT COALESCE(
               (SELECT ClazzLog.clazzLogUid
                  FROM ClazzLog
                 WHERE ClazzLog.clazzLogClazzUid = :clazzUid
                   AND (ClazzLog.clazzLogStatusFlag & ${ClazzLog.STATUS_RESCHEDULED}) != ${ClazzLog.STATUS_RESCHEDULED}
              ORDER BY ClazzLog.logDate DESC
                 LIMIT 1), 0)

        
    """)
    abstract suspend fun findMostRecentClazzLogToEditUid(
        clazzUid: Long
    ): Long



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entityList: List<ClazzLog>)



}
