package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ErrorReport
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class ErrorReportDao {

    @Query("""
 REPLACE INTO ErrorReportReplicate(erPk, erDestination)
  SELECT DISTINCT ErrorReport.errUid AS erUid,
           UserSession.usClientNodeId AS erDestination
    FROM ChangeLog
         JOIN ErrorReport
             ON ChangeLog.chTableId = 419
                AND ChangeLog.chEntityPk = ErrorReport.errUid
         JOIN UserSession ON UserSession.usSessionType = ${UserSession.TYPE_UPSTREAM}
    WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ErrorReport.errLct != COALESCE(
         (SELECT erVersionId
            FROM ErrorReportReplicate
           WHERE erPk = ErrorReport.errUid
             AND erDestination = UserSession.usClientNodeId), 0)
    /*psql ON CONFLICT(erPk, erDestination) DO UPDATE
     SET erPending = true
    */               
    """)
    @ReplicationRunOnChange([ErrorReport::class])
    @ReplicationCheckPendingNotificationsFor([ErrorReport::class])
    abstract suspend fun replicateOnChange()

    @Insert
    abstract suspend fun insertAsync(errorReport: ErrorReport): Long

    @Query("""
        SELECT ErrorReport.* 
          FROM ErrorReport
         WHERE errUid = :errUid
    """)
    abstract suspend fun findByUidAsync(errUid: Long): ErrorReport?

    @Query("""
        SELECT ErrorReport.*
          FROM ErrorReport
         WHERE errorCode = :errCode  
    """)
    abstract suspend fun findByErrorCode(errCode: Int): List<ErrorReport>

}