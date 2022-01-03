// @Triggers(arrayOf(
//     Trigger(
//         name = "statecontententity_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO StateContentEntity(stateContentUid, stateContentStateUid, stateContentKey, stateContentValue, isIsactive, stateContentMasterChangeSeqNum, stateContentLocalChangeSeqNum, stateContentLastChangedBy, stateContentLct) 
//             VALUES (NEW.stateContentUid, NEW.stateContentStateUid, NEW.stateContentKey, NEW.stateContentValue, NEW.isIsactive, NEW.stateContentMasterChangeSeqNum, NEW.stateContentLocalChangeSeqNum, NEW.stateContentLastChangedBy, NEW.stateContentLct) 
//             /*psql ON CONFLICT (stateContentUid) DO UPDATE 
//             SET stateContentStateUid = EXCLUDED.stateContentStateUid, stateContentKey = EXCLUDED.stateContentKey, stateContentValue = EXCLUDED.stateContentValue, isIsactive = EXCLUDED.isIsactive, stateContentMasterChangeSeqNum = EXCLUDED.stateContentMasterChangeSeqNum, stateContentLocalChangeSeqNum = EXCLUDED.stateContentLocalChangeSeqNum, stateContentLastChangedBy = EXCLUDED.stateContentLastChangedBy, stateContentLct = EXCLUDED.stateContentLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO StateContentEntityReplicate(scePk, sceVersionId, sceDestination)
//      SELECT StateContentEntity.stateContentUid AS sceUid,
//             StateContentEntity.stateContentLct AS sceVersionId,
//             :newNodeId AS sceDestination
//        FROM StateContentEntity
//       WHERE StateContentEntity.stateContentLct != COALESCE(
//             (SELECT sceVersionId
//                FROM StateContentEntityReplicate
//               WHERE scePk = StateContentEntity.stateContentUid
//                 AND sceDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(scePk, sceDestination) DO UPDATE
//             SET scePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([StateContentEntity::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO StateContentEntityReplicate(scePk, sceVersionId, sceDestination)
//  SELECT StateContentEntity.stateContentUid AS sceUid,
//         StateContentEntity.stateContentLct AS sceVersionId,
//         UserSession.usClientNodeId AS sceDestination
//    FROM ChangeLog
//         JOIN StateContentEntity
//             ON ChangeLog.chTableId = 72
//                AND ChangeLog.chEntityPk = StateContentEntity.stateContentUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND StateContentEntity.stateContentLct != COALESCE(
//         (SELECT sceVersionId
//            FROM StateContentEntityReplicate
//           WHERE scePk = StateContentEntity.stateContentUid
//             AND sceDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(scePk, sceDestination) DO UPDATE
//     SET scePending = true
//  */               
// """)
// @ReplicationRunOnChange([StateContentEntity::class])
// @ReplicationCheckPendingNotificationsFor([StateContentEntity::class])
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
  primaryKeys = arrayOf("scePk", "sceDestination"),
  indices = arrayOf(Index(value = arrayOf("scePk", "sceDestination", "sceVersionId")),
  Index(value = arrayOf("sceDestination", "scePending")))

)
@Serializable
public class StateContentEntityReplicate {
  @ReplicationEntityForeignKey
  public var scePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var sceVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var sceDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var scePending: Boolean = true
}
