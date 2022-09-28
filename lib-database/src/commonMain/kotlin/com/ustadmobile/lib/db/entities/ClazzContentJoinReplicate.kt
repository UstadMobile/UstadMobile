// @Triggers(arrayOf(
//     Trigger(
//         name = "clazzcontentjoin_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ClazzContentJoin(ccjUid, ccjContentEntryUid, ccjClazzUid, ccjActive, ccjLocalChangeSeqNum, ccjMasterChangeSeqNum, ccjLastChangedBy, ccjLct) 
//             VALUES (NEW.ccjUid, NEW.ccjContentEntryUid, NEW.ccjClazzUid, NEW.ccjActive, NEW.ccjLocalChangeSeqNum, NEW.ccjMasterChangeSeqNum, NEW.ccjLastChangedBy, NEW.ccjLct) 
//             /*psql ON CONFLICT (ccjUid) DO UPDATE 
//             SET ccjContentEntryUid = EXCLUDED.ccjContentEntryUid, ccjClazzUid = EXCLUDED.ccjClazzUid, ccjActive = EXCLUDED.ccjActive, ccjLocalChangeSeqNum = EXCLUDED.ccjLocalChangeSeqNum, ccjMasterChangeSeqNum = EXCLUDED.ccjMasterChangeSeqNum, ccjLastChangedBy = EXCLUDED.ccjLastChangedBy, ccjLct = EXCLUDED.ccjLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ClazzContentJoinReplicate(ccjPk, ccjVersionId, ccjDestination)
//      SELECT ClazzContentJoin.ccjUid AS ccjUid,
//             ClazzContentJoin.ccjLct AS ccjVersionId,
//             :newNodeId AS ccjDestination
//        FROM ClazzContentJoin
//       WHERE ClazzContentJoin.ccjLct != COALESCE(
//             (SELECT ccjVersionId
//                FROM ClazzContentJoinReplicate
//               WHERE ccjPk = ClazzContentJoin.ccjUid
//                 AND ccjDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(ccjPk, ccjDestination) DO UPDATE
//             SET ccjPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ClazzContentJoin::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ClazzContentJoinReplicate(ccjPk, ccjVersionId, ccjDestination)
//  SELECT ClazzContentJoin.ccjUid AS ccjUid,
//         ClazzContentJoin.ccjLct AS ccjVersionId,
//         UserSession.usClientNodeId AS ccjDestination
//    FROM ChangeLog
//         JOIN ClazzContentJoin
//             ON ChangeLog.chTableId = 134
//                AND ChangeLog.chEntityPk = ClazzContentJoin.ccjUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ClazzContentJoin.ccjLct != COALESCE(
//         (SELECT ccjVersionId
//            FROM ClazzContentJoinReplicate
//           WHERE ccjPk = ClazzContentJoin.ccjUid
//             AND ccjDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(ccjPk, ccjDestination) DO UPDATE
//     SET ccjPending = true
//  */               
// """)
// @ReplicationRunOnChange([ClazzContentJoin::class])
// @ReplicationCheckPendingNotificationsFor([ClazzContentJoin::class])
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
  primaryKeys = arrayOf("ccjPk", "ccjDestination"),
  indices = arrayOf(Index(value = arrayOf("ccjPk", "ccjDestination", "ccjVersionId")),
  Index(value = arrayOf("ccjDestination", "ccjPending")))

)
@Serializable
public class ClazzContentJoinReplicate {
  @ReplicationEntityForeignKey
  public var ccjPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var ccjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ccjDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var ccjPending: Boolean = true
}
