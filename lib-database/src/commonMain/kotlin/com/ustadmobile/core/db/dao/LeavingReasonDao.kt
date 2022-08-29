package com.ustadmobile.core.db.dao

import com.ustadmobile.door.paging.DataSourceFactory
import androidx.room.*
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

@Repository
@DoorDao
expect abstract class LeavingReasonDao : BaseDao<LeavingReason> {

    @Query("""
         REPLACE INTO LeavingReasonReplicate(lrPk, lrDestination)
          SELECT DISTINCT LeavingReason.leavingReasonUid AS lrPk,
                 :newNodeId AS lrDestination
            FROM LeavingReason
           WHERE LeavingReason.leavingReasonLct != COALESCE(
                 (SELECT lrVersionId
                    FROM LeavingReasonReplicate
                   WHERE lrPk = LeavingReason.leavingReasonUid
                     AND lrDestination = :newNodeId), 0) 
          /*psql ON CONFLICT(lrPk, lrDestination) DO UPDATE
                 SET lrPending = true
          */       
     """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([LeavingReason::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO LeavingReasonReplicate(lrPk, lrDestination)
  SELECT DISTINCT LeavingReason.leavingReasonUid AS lrUid,
         UserSession.usClientNodeId AS lrDestination
    FROM ChangeLog
         JOIN LeavingReason
              ON ChangeLog.chTableId = ${LeavingReason.TABLE_ID}
                 AND ChangeLog.chEntityPk = LeavingReason.leavingReasonUid
         JOIN UserSession 
              ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND LeavingReason.leavingReasonLct != COALESCE(
         (SELECT lrVersionId
            FROM LeavingReasonReplicate
           WHERE lrPk = LeavingReason.leavingReasonUid
             AND lrDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(lrPk, lrDestination) DO UPDATE
     SET lrPending = true
  */               
    """)
    @ReplicationRunOnChange([LeavingReason::class])
    @ReplicationCheckPendingNotificationsFor([LeavingReason::class])
    abstract suspend fun replicateOnChange()

    @Query("""SELECT * FROM LeavingReason""")
    abstract fun findAllReasons(): DataSourceFactory<Int, LeavingReason>

    @Query("SELECT * FROM LeavingReason")
    abstract fun findAllReasonsLive(): List<LeavingReason>

    @JsName("findByUid")
    @Query("SELECT * FROM LeavingReason WHERE leavingReasonUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): LeavingReason?

    @JsName("findByUidList")
    @Query("SELECT leavingReasonUid FROM LeavingReason WHERE leavingReasonUid IN (:uidList)")
    abstract suspend fun findByUidList(uidList: List<Long>): List<Long>

    @JsName("findByUidLive")
    @Query("SELECT * FROM LeavingReason WHERE leavingReasonUid = :uid")
    abstract fun findByUidLive(uid: Long): LiveData<LeavingReason?>

    @JsName("getReasonsFromUids")
    @Query("""SELECT LeavingReason.leavingReasonUid AS uid, 
            LeavingReason.leavingReasonTitle As labelName  
            FROM LeavingReason WHERE leavingReasonUid IN (:uidList)""")
    abstract suspend fun getReasonsFromUids(uidList: List<Long>): List<UidAndLabel>

    @JsName("replaceList")
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceList(entityList: List<LeavingReason>)

    @Update
    abstract suspend fun updateAsync(entity: LeavingReason): Int


}