// @Triggers(arrayOf(
//     Trigger(
//         name = "contententryparentchildjoin_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ContentEntryParentChildJoin(cepcjParentContentEntryUid, cepcjChildContentEntryUid, childIndex, cepcjUid, cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum, cepcjLastChangedBy, cepcjLct) 
//             VALUES (NEW.cepcjParentContentEntryUid, NEW.cepcjChildContentEntryUid, NEW.childIndex, NEW.cepcjUid, NEW.cepcjLocalChangeSeqNum, NEW.cepcjMasterChangeSeqNum, NEW.cepcjLastChangedBy, NEW.cepcjLct) 
//             /*psql ON CONFLICT (cepcjUid) DO UPDATE 
//             SET cepcjParentContentEntryUid = EXCLUDED.cepcjParentContentEntryUid, cepcjChildContentEntryUid = EXCLUDED.cepcjChildContentEntryUid, childIndex = EXCLUDED.childIndex, cepcjLocalChangeSeqNum = EXCLUDED.cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum = EXCLUDED.cepcjMasterChangeSeqNum, cepcjLastChangedBy = EXCLUDED.cepcjLastChangedBy, cepcjLct = EXCLUDED.cepcjLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ContentEntryParentChildJoinReplicate(cepcjPk, cepcjVersionId, cepcjDestination)
//      SELECT ContentEntryParentChildJoin.cepcjUid AS cepcjUid,
//             ContentEntryParentChildJoin.cepcjLct AS cepcjVersionId,
//             :newNodeId AS cepcjDestination
//        FROM ContentEntryParentChildJoin
//       WHERE ContentEntryParentChildJoin.cepcjLct != COALESCE(
//             (SELECT cepcjVersionId
//                FROM ContentEntryParentChildJoinReplicate
//               WHERE cepcjPk = ContentEntryParentChildJoin.cepcjUid
//                 AND cepcjDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(cepcjPk, cepcjDestination) DO UPDATE
//             SET cepcjPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ContentEntryParentChildJoin::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ContentEntryParentChildJoinReplicate(cepcjPk, cepcjVersionId, cepcjDestination)
//  SELECT ContentEntryParentChildJoin.cepcjUid AS cepcjUid,
//         ContentEntryParentChildJoin.cepcjLct AS cepcjVersionId,
//         UserSession.usClientNodeId AS cepcjDestination
//    FROM ChangeLog
//         JOIN ContentEntryParentChildJoin
//             ON ChangeLog.chTableId = 7
//                AND ChangeLog.chEntityPk = ContentEntryParentChildJoin.cepcjUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ContentEntryParentChildJoin.cepcjLct != COALESCE(
//         (SELECT cepcjVersionId
//            FROM ContentEntryParentChildJoinReplicate
//           WHERE cepcjPk = ContentEntryParentChildJoin.cepcjUid
//             AND cepcjDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(cepcjPk, cepcjDestination) DO UPDATE
//     SET cepcjPending = true
//  */               
// """)
// @ReplicationRunOnChange([ContentEntryParentChildJoin::class])
// @ReplicationCheckPendingNotificationsFor([ContentEntryParentChildJoin::class])
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
  primaryKeys = arrayOf("cepcjPk", "cepcjDestination"),
  indices = arrayOf(Index(value = arrayOf("cepcjPk", "cepcjDestination", "cepcjVersionId")),
  Index(value = arrayOf("cepcjDestination", "cepcjPending")))

)
@Serializable
public class ContentEntryParentChildJoinReplicate {
  @ReplicationEntityForeignKey
  public var cepcjPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var cepcjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cepcjDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var cepcjPending: Boolean = true
}
