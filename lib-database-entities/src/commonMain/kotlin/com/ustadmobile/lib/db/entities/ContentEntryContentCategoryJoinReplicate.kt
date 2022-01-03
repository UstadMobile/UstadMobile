// @Triggers(arrayOf(
//     Trigger(
//         name = "contententrycontentcategoryjoin_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ContentEntryContentCategoryJoin(ceccjUid, ceccjContentEntryUid, ceccjContentCategoryUid, ceccjLocalChangeSeqNum, ceccjMasterChangeSeqNum, ceccjLastChangedBy, ceccjLct) 
//             VALUES (NEW.ceccjUid, NEW.ceccjContentEntryUid, NEW.ceccjContentCategoryUid, NEW.ceccjLocalChangeSeqNum, NEW.ceccjMasterChangeSeqNum, NEW.ceccjLastChangedBy, NEW.ceccjLct) 
//             /*psql ON CONFLICT (ceccjUid) DO UPDATE 
//             SET ceccjContentEntryUid = EXCLUDED.ceccjContentEntryUid, ceccjContentCategoryUid = EXCLUDED.ceccjContentCategoryUid, ceccjLocalChangeSeqNum = EXCLUDED.ceccjLocalChangeSeqNum, ceccjMasterChangeSeqNum = EXCLUDED.ceccjMasterChangeSeqNum, ceccjLastChangedBy = EXCLUDED.ceccjLastChangedBy, ceccjLct = EXCLUDED.ceccjLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ContentEntryContentCategoryJoinReplicate(ceccjPk, ceccjVersionId, ceccjDestination)
//      SELECT ContentEntryContentCategoryJoin.ceccjUid AS ceccjUid,
//             ContentEntryContentCategoryJoin.ceccjLct AS ceccjVersionId,
//             :newNodeId AS ceccjDestination
//        FROM ContentEntryContentCategoryJoin
//       WHERE ContentEntryContentCategoryJoin.ceccjLct != COALESCE(
//             (SELECT ceccjVersionId
//                FROM ContentEntryContentCategoryJoinReplicate
//               WHERE ceccjPk = ContentEntryContentCategoryJoin.ceccjUid
//                 AND ceccjDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(ceccjPk, ceccjDestination) DO UPDATE
//             SET ceccjPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ContentEntryContentCategoryJoin::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ContentEntryContentCategoryJoinReplicate(ceccjPk, ceccjVersionId, ceccjDestination)
//  SELECT ContentEntryContentCategoryJoin.ceccjUid AS ceccjUid,
//         ContentEntryContentCategoryJoin.ceccjLct AS ceccjVersionId,
//         UserSession.usClientNodeId AS ceccjDestination
//    FROM ChangeLog
//         JOIN ContentEntryContentCategoryJoin
//             ON ChangeLog.chTableId = 3
//                AND ChangeLog.chEntityPk = ContentEntryContentCategoryJoin.ceccjUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ContentEntryContentCategoryJoin.ceccjLct != COALESCE(
//         (SELECT ceccjVersionId
//            FROM ContentEntryContentCategoryJoinReplicate
//           WHERE ceccjPk = ContentEntryContentCategoryJoin.ceccjUid
//             AND ceccjDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(ceccjPk, ceccjDestination) DO UPDATE
//     SET ceccjPending = true
//  */               
// """)
// @ReplicationRunOnChange([ContentEntryContentCategoryJoin::class])
// @ReplicationCheckPendingNotificationsFor([ContentEntryContentCategoryJoin::class])
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
  primaryKeys = arrayOf("ceccjPk", "ceccjDestination"),
  indices = arrayOf(Index(value = arrayOf("ceccjPk", "ceccjDestination", "ceccjVersionId")),
  Index(value = arrayOf("ceccjDestination", "ceccjPending")))

)
@Serializable
public class ContentEntryContentCategoryJoinReplicate {
  @ReplicationEntityForeignKey
  public var ceccjPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var ceccjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ceccjDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var ceccjPending: Boolean = true
}
