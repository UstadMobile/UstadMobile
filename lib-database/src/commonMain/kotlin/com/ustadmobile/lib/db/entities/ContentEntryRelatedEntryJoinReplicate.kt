// @Triggers(arrayOf(
//     Trigger(
//         name = "contententryrelatedentryjoin_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ContentEntryRelatedEntryJoin(cerejUid, cerejContentEntryUid, cerejRelatedEntryUid, cerejLastChangedBy, relType, comment, cerejRelLanguageUid, cerejLocalChangeSeqNum, cerejMasterChangeSeqNum, cerejLct) 
//             VALUES (NEW.cerejUid, NEW.cerejContentEntryUid, NEW.cerejRelatedEntryUid, NEW.cerejLastChangedBy, NEW.relType, NEW.comment, NEW.cerejRelLanguageUid, NEW.cerejLocalChangeSeqNum, NEW.cerejMasterChangeSeqNum, NEW.cerejLct) 
//             /*psql ON CONFLICT (cerejUid) DO UPDATE 
//             SET cerejContentEntryUid = EXCLUDED.cerejContentEntryUid, cerejRelatedEntryUid = EXCLUDED.cerejRelatedEntryUid, cerejLastChangedBy = EXCLUDED.cerejLastChangedBy, relType = EXCLUDED.relType, comment = EXCLUDED.comment, cerejRelLanguageUid = EXCLUDED.cerejRelLanguageUid, cerejLocalChangeSeqNum = EXCLUDED.cerejLocalChangeSeqNum, cerejMasterChangeSeqNum = EXCLUDED.cerejMasterChangeSeqNum, cerejLct = EXCLUDED.cerejLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ContentEntryRelatedEntryJoinReplicate(cerejPk, cerejVersionId, cerejDestination)
//      SELECT ContentEntryRelatedEntryJoin.cerejUid AS cerejUid,
//             ContentEntryRelatedEntryJoin.cerejLct AS cerejVersionId,
//             :newNodeId AS cerejDestination
//        FROM ContentEntryRelatedEntryJoin
//       WHERE ContentEntryRelatedEntryJoin.cerejLct != COALESCE(
//             (SELECT cerejVersionId
//                FROM ContentEntryRelatedEntryJoinReplicate
//               WHERE cerejPk = ContentEntryRelatedEntryJoin.cerejUid
//                 AND cerejDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(cerejPk, cerejDestination) DO UPDATE
//             SET cerejPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ContentEntryRelatedEntryJoin::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ContentEntryRelatedEntryJoinReplicate(cerejPk, cerejVersionId, cerejDestination)
//  SELECT ContentEntryRelatedEntryJoin.cerejUid AS cerejUid,
//         ContentEntryRelatedEntryJoin.cerejLct AS cerejVersionId,
//         UserSession.usClientNodeId AS cerejDestination
//    FROM ChangeLog
//         JOIN ContentEntryRelatedEntryJoin
//             ON ChangeLog.chTableId = 8
//                AND ChangeLog.chEntityPk = ContentEntryRelatedEntryJoin.cerejUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ContentEntryRelatedEntryJoin.cerejLct != COALESCE(
//         (SELECT cerejVersionId
//            FROM ContentEntryRelatedEntryJoinReplicate
//           WHERE cerejPk = ContentEntryRelatedEntryJoin.cerejUid
//             AND cerejDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(cerejPk, cerejDestination) DO UPDATE
//     SET cerejPending = true
//  */               
// """)
// @ReplicationRunOnChange([ContentEntryRelatedEntryJoin::class])
// @ReplicationCheckPendingNotificationsFor([ContentEntryRelatedEntryJoin::class])
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
  primaryKeys = arrayOf("cerejPk", "cerejDestination"),
  indices = arrayOf(Index(value = arrayOf("cerejPk", "cerejDestination", "cerejVersionId")),
  Index(value = arrayOf("cerejDestination", "cerejPending")))

)
@Serializable
public class ContentEntryRelatedEntryJoinReplicate {
  @ReplicationEntityForeignKey
  public var cerejPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var cerejVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cerejDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var cerejPending: Boolean = true
}
