// @Triggers(arrayOf(
//     Trigger(
//         name = "leavingreason_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO LeavingReason(leavingReasonUid, leavingReasonTitle, leavingReasonMCSN, leavingReasonCSN, leavingReasonLCB, leavingReasonLct) 
//             VALUES (NEW.leavingReasonUid, NEW.leavingReasonTitle, NEW.leavingReasonMCSN, NEW.leavingReasonCSN, NEW.leavingReasonLCB, NEW.leavingReasonLct) 
//             /*psql ON CONFLICT (leavingReasonUid) DO UPDATE 
//             SET leavingReasonTitle = EXCLUDED.leavingReasonTitle, leavingReasonMCSN = EXCLUDED.leavingReasonMCSN, leavingReasonCSN = EXCLUDED.leavingReasonCSN, leavingReasonLCB = EXCLUDED.leavingReasonLCB, leavingReasonLct = EXCLUDED.leavingReasonLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO LeavingReasonReplicate(lrPk, lrVersionId, lrDestination)
//      SELECT LeavingReason.leavingReasonUid AS lrUid,
//             LeavingReason.leavingReasonLct AS lrVersionId,
//             :newNodeId AS lrDestination
//        FROM LeavingReason
//       WHERE LeavingReason.leavingReasonLct != COALESCE(
//             (SELECT lrVersionId
//                FROM LeavingReasonReplicate
//               WHERE lrPk = LeavingReason.leavingReasonUid
//                 AND lrDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(lrPk, lrDestination) DO UPDATE
//             SET lrPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([LeavingReason::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO LeavingReasonReplicate(lrPk, lrVersionId, lrDestination)
//  SELECT LeavingReason.leavingReasonUid AS lrUid,
//         LeavingReason.leavingReasonLct AS lrVersionId,
//         UserSession.usClientNodeId AS lrDestination
//    FROM ChangeLog
//         JOIN LeavingReason
//             ON ChangeLog.chTableId = 410
//                AND ChangeLog.chEntityPk = LeavingReason.leavingReasonUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND LeavingReason.leavingReasonLct != COALESCE(
//         (SELECT lrVersionId
//            FROM LeavingReasonReplicate
//           WHERE lrPk = LeavingReason.leavingReasonUid
//             AND lrDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(lrPk, lrDestination) DO UPDATE
//     SET lrPending = true
//  */               
// """)
// @ReplicationRunOnChange([LeavingReason::class])
// @ReplicationCheckPendingNotificationsFor([LeavingReason::class])
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
  primaryKeys = arrayOf("lrPk", "lrDestination"),
  indices = arrayOf(Index(value = arrayOf("lrPk", "lrDestination", "lrVersionId")),
  Index(value = arrayOf("lrDestination", "lrPending")))

)
@Serializable
public class LeavingReasonReplicate {
  @ReplicationEntityForeignKey
  public var lrPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var lrVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var lrDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var lrPending: Boolean = true
}
