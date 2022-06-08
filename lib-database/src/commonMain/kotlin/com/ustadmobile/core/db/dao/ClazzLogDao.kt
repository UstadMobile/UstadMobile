package com.ustadmobile.core.db.dao

import com.ustadmobile.door.DoorDataSourceFactory
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Role


@Repository
@Dao
abstract class ClazzLogDao : BaseDao<ClazzLog> {

    @Query("""
     REPLACE INTO ClazzLogReplicate(clPk, clDestination)
      SELECT DISTINCT ClazzLog.clazzLogUid AS clUid,
             :newNodeId AS clDestination
        FROM UserSession
             JOIN PersonGroupMember 
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                  ${Role.PERMISSION_CLAZZ_SELECT} 
                  ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
             JOIN ClazzLog
                  ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid
       WHERE ClazzLog.clazzLogLastChangedTime != COALESCE(
             (SELECT clVersionId
                FROM ClazzLogReplicate
               WHERE clPk = ClazzLog.clazzLogUid
                 AND clDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(clPk, clDestination) DO UPDATE
             SET clPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ClazzLog::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replace(entity: ClazzLog): Long

    @Query("""
 REPLACE INTO ClazzLogReplicate(clPk, clDestination)
  SELECT DISTINCT ClazzLog.clazzLogUid AS clUid,
         UserSession.usClientNodeId AS clDestination
    FROM ChangeLog
         JOIN ClazzLog 
              ON ChangeLog.chTableId = ${ClazzLog.TABLE_ID} 
             AND ClazzLog.clazzLogUid = ChangeLog.chEntityPk
         JOIN Clazz 
              ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid 
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_CLAZZ_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ClazzLog.clazzLogLastChangedTime != COALESCE(
         (SELECT clVersionId
            FROM ClazzLogReplicate
           WHERE clPk = ClazzLog.clazzLogUid
             AND clDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(clPk, clDestination) DO UPDATE
     SET clPending = true
  */               
    """)
    @ReplicationRunOnChange([ClazzLog::class])
    @ReplicationCheckPendingNotificationsFor([ClazzLog::class])
    abstract suspend fun replicateOnChange()

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
        clazzLogLastChangedTime = :timeChanged
        WHERE clazzLogUid = :clazzLogUid""")
    abstract fun updateStatusByClazzLogUid(clazzLogUid: Long, newStatus: Int, timeChanged: Long)

    @Update
    abstract suspend fun updateAsync(clazzLog: ClazzLog)

}
