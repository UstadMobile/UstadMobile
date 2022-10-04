// @Triggers(arrayOf(
//     Trigger(
//         name = "report_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO Report(reportUid, reportOwnerUid, xAxis, reportDateRangeSelection, fromDate, fromRelTo, fromRelOffSet, fromRelUnit, toDate, toRelTo, toRelOffSet, toRelUnit, reportTitle, reportDescription, reportSeries, reportInactive, isTemplate, priority, reportTitleId, reportDescId, reportMasterChangeSeqNum, reportLocalChangeSeqNum, reportLastChangedBy, reportLct) 
//             VALUES (NEW.reportUid, NEW.reportOwnerUid, NEW.xAxis, NEW.reportDateRangeSelection, NEW.fromDate, NEW.fromRelTo, NEW.fromRelOffSet, NEW.fromRelUnit, NEW.toDate, NEW.toRelTo, NEW.toRelOffSet, NEW.toRelUnit, NEW.reportTitle, NEW.reportDescription, NEW.reportSeries, NEW.reportInactive, NEW.isTemplate, NEW.priority, NEW.reportTitleId, NEW.reportDescId, NEW.reportMasterChangeSeqNum, NEW.reportLocalChangeSeqNum, NEW.reportLastChangedBy, NEW.reportLct) 
//             /*psql ON CONFLICT (reportUid) DO UPDATE 
//             SET reportOwnerUid = EXCLUDED.reportOwnerUid, xAxis = EXCLUDED.xAxis, reportDateRangeSelection = EXCLUDED.reportDateRangeSelection, fromDate = EXCLUDED.fromDate, fromRelTo = EXCLUDED.fromRelTo, fromRelOffSet = EXCLUDED.fromRelOffSet, fromRelUnit = EXCLUDED.fromRelUnit, toDate = EXCLUDED.toDate, toRelTo = EXCLUDED.toRelTo, toRelOffSet = EXCLUDED.toRelOffSet, toRelUnit = EXCLUDED.toRelUnit, reportTitle = EXCLUDED.reportTitle, reportDescription = EXCLUDED.reportDescription, reportSeries = EXCLUDED.reportSeries, reportInactive = EXCLUDED.reportInactive, isTemplate = EXCLUDED.isTemplate, priority = EXCLUDED.priority, reportTitleId = EXCLUDED.reportTitleId, reportDescId = EXCLUDED.reportDescId, reportMasterChangeSeqNum = EXCLUDED.reportMasterChangeSeqNum, reportLocalChangeSeqNum = EXCLUDED.reportLocalChangeSeqNum, reportLastChangedBy = EXCLUDED.reportLastChangedBy, reportLct = EXCLUDED.reportLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ReportReplicate(reportPk, reportVersionId, reportDestination)
//      SELECT Report.reportUid AS reportUid,
//             Report.reportLct AS reportVersionId,
//             :newNodeId AS reportDestination
//        FROM Report
//       WHERE Report.reportLct != COALESCE(
//             (SELECT reportVersionId
//                FROM ReportReplicate
//               WHERE reportPk = Report.reportUid
//                 AND reportDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(reportPk, reportDestination) DO UPDATE
//             SET reportPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([Report::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ReportReplicate(reportPk, reportVersionId, reportDestination)
//  SELECT Report.reportUid AS reportUid,
//         Report.reportLct AS reportVersionId,
//         UserSession.usClientNodeId AS reportDestination
//    FROM ChangeLog
//         JOIN Report
//             ON ChangeLog.chTableId = 101
//                AND ChangeLog.chEntityPk = Report.reportUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND Report.reportLct != COALESCE(
//         (SELECT reportVersionId
//            FROM ReportReplicate
//           WHERE reportPk = Report.reportUid
//             AND reportDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(reportPk, reportDestination) DO UPDATE
//     SET reportPending = true
//  */               
// """)
// @ReplicationRunOnChange([Report::class])
// @ReplicationCheckPendingNotificationsFor([Report::class])
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
  primaryKeys = arrayOf("reportPk", "reportDestination"),
  indices = arrayOf(Index(value = arrayOf("reportPk", "reportDestination", "reportVersionId")),
  Index(value = arrayOf("reportDestination", "reportPending")))

)
@Serializable
public class ReportReplicate {
  @ReplicationEntityForeignKey
  public var reportPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var reportVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var reportDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var reportPending: Boolean = true
}
