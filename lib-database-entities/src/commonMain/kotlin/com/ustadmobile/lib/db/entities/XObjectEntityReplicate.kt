// @Triggers(arrayOf(
//     Trigger(
//         name = "xobjectentity_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO XObjectEntity(xObjectUid, objectType, objectId, definitionType, interactionType, correctResponsePattern, objectContentEntryUid, xObjectMasterChangeSeqNum, xObjectocalChangeSeqNum, xObjectLastChangedBy, xObjectLct) 
//             VALUES (NEW.xObjectUid, NEW.objectType, NEW.objectId, NEW.definitionType, NEW.interactionType, NEW.correctResponsePattern, NEW.objectContentEntryUid, NEW.xObjectMasterChangeSeqNum, NEW.xObjectocalChangeSeqNum, NEW.xObjectLastChangedBy, NEW.xObjectLct) 
//             /*psql ON CONFLICT (xObjectUid) DO UPDATE 
//             SET objectType = EXCLUDED.objectType, objectId = EXCLUDED.objectId, definitionType = EXCLUDED.definitionType, interactionType = EXCLUDED.interactionType, correctResponsePattern = EXCLUDED.correctResponsePattern, objectContentEntryUid = EXCLUDED.objectContentEntryUid, xObjectMasterChangeSeqNum = EXCLUDED.xObjectMasterChangeSeqNum, xObjectocalChangeSeqNum = EXCLUDED.xObjectocalChangeSeqNum, xObjectLastChangedBy = EXCLUDED.xObjectLastChangedBy, xObjectLct = EXCLUDED.xObjectLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO XObjectEntityReplicate(xoePk, xoeVersionId, xoeDestination)
//      SELECT XObjectEntity.xObjectUid AS xoeUid,
//             XObjectEntity.xObjectLct AS xoeVersionId,
//             :newNodeId AS xoeDestination
//        FROM XObjectEntity
//       WHERE XObjectEntity.xObjectLct != COALESCE(
//             (SELECT xoeVersionId
//                FROM XObjectEntityReplicate
//               WHERE xoePk = XObjectEntity.xObjectUid
//                 AND xoeDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(xoePk, xoeDestination) DO UPDATE
//             SET xoePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([XObjectEntity::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO XObjectEntityReplicate(xoePk, xoeVersionId, xoeDestination)
//  SELECT XObjectEntity.xObjectUid AS xoeUid,
//         XObjectEntity.xObjectLct AS xoeVersionId,
//         UserSession.usClientNodeId AS xoeDestination
//    FROM ChangeLog
//         JOIN XObjectEntity
//             ON ChangeLog.chTableId = 64
//                AND ChangeLog.chEntityPk = XObjectEntity.xObjectUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND XObjectEntity.xObjectLct != COALESCE(
//         (SELECT xoeVersionId
//            FROM XObjectEntityReplicate
//           WHERE xoePk = XObjectEntity.xObjectUid
//             AND xoeDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(xoePk, xoeDestination) DO UPDATE
//     SET xoePending = true
//  */               
// """)
// @ReplicationRunOnChange([XObjectEntity::class])
// @ReplicationCheckPendingNotificationsFor([XObjectEntity::class])
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
  primaryKeys = arrayOf("xoePk", "xoeDestination"),
  indices = arrayOf(Index(value = arrayOf("xoePk", "xoeDestination", "xoeVersionId")),
  Index(value = arrayOf("xoeDestination", "xoePending")))

)
@Serializable
public class XObjectEntityReplicate {
  @ReplicationEntityForeignKey
  public var xoePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var xoeVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var xoeDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var xoePending: Boolean = true
}
