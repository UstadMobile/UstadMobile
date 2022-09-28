// @Triggers(arrayOf(
//     Trigger(
//         name = "errorreport_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ErrorReport(errUid, errPcsn, errLcsn, errLcb, errLct, severity, timestamp, presenterUri, appVersion, versionCode, errorCode, operatingSys, osVersion, stackTrace, message) 
//             VALUES (NEW.errUid, NEW.errPcsn, NEW.errLcsn, NEW.errLcb, NEW.errLct, NEW.severity, NEW.timestamp, NEW.presenterUri, NEW.appVersion, NEW.versionCode, NEW.errorCode, NEW.operatingSys, NEW.osVersion, NEW.stackTrace, NEW.message) 
//             /*psql ON CONFLICT (errUid) DO UPDATE 
//             SET errPcsn = EXCLUDED.errPcsn, errLcsn = EXCLUDED.errLcsn, errLcb = EXCLUDED.errLcb, errLct = EXCLUDED.errLct, severity = EXCLUDED.severity, timestamp = EXCLUDED.timestamp, presenterUri = EXCLUDED.presenterUri, appVersion = EXCLUDED.appVersion, versionCode = EXCLUDED.versionCode, errorCode = EXCLUDED.errorCode, operatingSys = EXCLUDED.operatingSys, osVersion = EXCLUDED.osVersion, stackTrace = EXCLUDED.stackTrace, message = EXCLUDED.message
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ErrorReportReplicate(erPk, erVersionId, erDestination)
//      SELECT ErrorReport.errUid AS erUid,
//             ErrorReport.errLct AS erVersionId,
//             :newNodeId AS erDestination
//        FROM ErrorReport
//       WHERE ErrorReport.errLct != COALESCE(
//             (SELECT erVersionId
//                FROM ErrorReportReplicate
//               WHERE erPk = ErrorReport.errUid
//                 AND erDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(erPk, erDestination) DO UPDATE
//             SET erPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ErrorReport::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ErrorReportReplicate(erPk, erVersionId, erDestination)
//  SELECT ErrorReport.errUid AS erUid,
//         ErrorReport.errLct AS erVersionId,
//         UserSession.usClientNodeId AS erDestination
//    FROM ChangeLog
//         JOIN ErrorReport
//             ON ChangeLog.chTableId = 419
//                AND ChangeLog.chEntityPk = ErrorReport.errUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ErrorReport.errLct != COALESCE(
//         (SELECT erVersionId
//            FROM ErrorReportReplicate
//           WHERE erPk = ErrorReport.errUid
//             AND erDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(erPk, erDestination) DO UPDATE
//     SET erPending = true
//  */               
// """)
// @ReplicationRunOnChange([ErrorReport::class])
// @ReplicationCheckPendingNotificationsFor([ErrorReport::class])
// abstract suspend fun replicateOnChange()
package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.`annotation`.ReplicationDestinationNodeId
import com.ustadmobile.door.`annotation`.ReplicationEntityForeignKey
import com.ustadmobile.door.`annotation`.ReplicationPending
import com.ustadmobile.door.`annotation`.ReplicationVersionId
import kotlin.Boolean
import kotlin.Long
import kotlinx.serialization.Serializable

@Entity(
  primaryKeys = arrayOf("erPk", "erDestination"),
  indices = arrayOf(Index(value = arrayOf("erPk", "erDestination", "erVersionId")),
  Index(value = arrayOf("erDestination", "erPending")))

)
@Serializable
public class ErrorReportReplicate {
  @ReplicationEntityForeignKey
  public var erPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var erVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var erDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var erPending: Boolean = true
}
