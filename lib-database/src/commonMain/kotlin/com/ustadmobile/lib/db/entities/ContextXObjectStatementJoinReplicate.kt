// @Triggers(arrayOf(
//     Trigger(
//         name = "contextxobjectstatementjoin_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ContextXObjectStatementJoin(contextXObjectStatementJoinUid, contextActivityFlag, contextStatementUid, contextXObjectUid, verbMasterChangeSeqNum, verbLocalChangeSeqNum, verbLastChangedBy, contextXObjectLct) 
//             VALUES (NEW.contextXObjectStatementJoinUid, NEW.contextActivityFlag, NEW.contextStatementUid, NEW.contextXObjectUid, NEW.verbMasterChangeSeqNum, NEW.verbLocalChangeSeqNum, NEW.verbLastChangedBy, NEW.contextXObjectLct) 
//             /*psql ON CONFLICT (contextXObjectStatementJoinUid) DO UPDATE 
//             SET contextActivityFlag = EXCLUDED.contextActivityFlag, contextStatementUid = EXCLUDED.contextStatementUid, contextXObjectUid = EXCLUDED.contextXObjectUid, verbMasterChangeSeqNum = EXCLUDED.verbMasterChangeSeqNum, verbLocalChangeSeqNum = EXCLUDED.verbLocalChangeSeqNum, verbLastChangedBy = EXCLUDED.verbLastChangedBy, contextXObjectLct = EXCLUDED.contextXObjectLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ContextXObjectStatementJoinReplicate(cxosjPk, cxosjVersionId, cxosjDestination)
//      SELECT ContextXObjectStatementJoin.contextXObjectStatementJoinUid AS cxosjUid,
//             ContextXObjectStatementJoin.contextXObjectLct AS cxosjVersionId,
//             :newNodeId AS cxosjDestination
//        FROM ContextXObjectStatementJoin
//       WHERE ContextXObjectStatementJoin.contextXObjectLct != COALESCE(
//             (SELECT cxosjVersionId
//                FROM ContextXObjectStatementJoinReplicate
//               WHERE cxosjPk = ContextXObjectStatementJoin.contextXObjectStatementJoinUid
//                 AND cxosjDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(cxosjPk, cxosjDestination) DO UPDATE
//             SET cxosjPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ContextXObjectStatementJoin::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ContextXObjectStatementJoinReplicate(cxosjPk, cxosjVersionId, cxosjDestination)
//  SELECT ContextXObjectStatementJoin.contextXObjectStatementJoinUid AS cxosjUid,
//         ContextXObjectStatementJoin.contextXObjectLct AS cxosjVersionId,
//         UserSession.usClientNodeId AS cxosjDestination
//    FROM ChangeLog
//         JOIN ContextXObjectStatementJoin
//             ON ChangeLog.chTableId = 66
//                AND ChangeLog.chEntityPk = ContextXObjectStatementJoin.contextXObjectStatementJoinUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ContextXObjectStatementJoin.contextXObjectLct != COALESCE(
//         (SELECT cxosjVersionId
//            FROM ContextXObjectStatementJoinReplicate
//           WHERE cxosjPk = ContextXObjectStatementJoin.contextXObjectStatementJoinUid
//             AND cxosjDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(cxosjPk, cxosjDestination) DO UPDATE
//     SET cxosjPending = true
//  */               
// """)
// @ReplicationRunOnChange([ContextXObjectStatementJoin::class])
// @ReplicationCheckPendingNotificationsFor([ContextXObjectStatementJoin::class])
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
  primaryKeys = arrayOf("cxosjPk", "cxosjDestination"),
  indices = arrayOf(Index(value = arrayOf("cxosjPk", "cxosjDestination", "cxosjVersionId")),
  Index(value = arrayOf("cxosjDestination", "cxosjPending")))

)
@Serializable
public class ContextXObjectStatementJoinReplicate {
  @ReplicationEntityForeignKey
  public var cxosjPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var cxosjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cxosjDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var cxosjPending: Boolean = true
}
