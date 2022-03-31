// @Triggers(arrayOf(
//     Trigger(
//         name = "stateentity_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO StateEntity(stateUid, stateId, agentUid, activityId, registration, isIsactive, timestamp, stateMasterChangeSeqNum, stateLocalChangeSeqNum, stateLastChangedBy, stateLct) 
//             VALUES (NEW.stateUid, NEW.stateId, NEW.agentUid, NEW.activityId, NEW.registration, NEW.isIsactive, NEW.timestamp, NEW.stateMasterChangeSeqNum, NEW.stateLocalChangeSeqNum, NEW.stateLastChangedBy, NEW.stateLct) 
//             /*psql ON CONFLICT (stateUid) DO UPDATE 
//             SET stateId = EXCLUDED.stateId, agentUid = EXCLUDED.agentUid, activityId = EXCLUDED.activityId, registration = EXCLUDED.registration, isIsactive = EXCLUDED.isIsactive, timestamp = EXCLUDED.timestamp, stateMasterChangeSeqNum = EXCLUDED.stateMasterChangeSeqNum, stateLocalChangeSeqNum = EXCLUDED.stateLocalChangeSeqNum, stateLastChangedBy = EXCLUDED.stateLastChangedBy, stateLct = EXCLUDED.stateLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO StateEntityReplicate(sePk, seVersionId, seDestination)
//      SELECT StateEntity.stateUid AS seUid,
//             StateEntity.stateLct AS seVersionId,
//             :newNodeId AS seDestination
//        FROM StateEntity
//       WHERE StateEntity.stateLct != COALESCE(
//             (SELECT seVersionId
//                FROM StateEntityReplicate
//               WHERE sePk = StateEntity.stateUid
//                 AND seDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(sePk, seDestination) DO UPDATE
//             SET sePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([StateEntity::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO StateEntityReplicate(sePk, seVersionId, seDestination)
//  SELECT StateEntity.stateUid AS seUid,
//         StateEntity.stateLct AS seVersionId,
//         UserSession.usClientNodeId AS seDestination
//    FROM ChangeLog
//         JOIN StateEntity
//             ON ChangeLog.chTableId = 70
//                AND ChangeLog.chEntityPk = StateEntity.stateUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND StateEntity.stateLct != COALESCE(
//         (SELECT seVersionId
//            FROM StateEntityReplicate
//           WHERE sePk = StateEntity.stateUid
//             AND seDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(sePk, seDestination) DO UPDATE
//     SET sePending = true
//  */               
// """)
// @ReplicationRunOnChange([StateEntity::class])
// @ReplicationCheckPendingNotificationsFor([StateEntity::class])
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
  primaryKeys = arrayOf("sePk", "seDestination"),
  indices = arrayOf(Index(value = arrayOf("sePk", "seDestination", "seVersionId")),
  Index(value = arrayOf("seDestination", "sePending")))

)
@Serializable
public class StateEntityReplicate {
  @ReplicationEntityForeignKey
  public var sePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var seVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var seDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var sePending: Boolean = true
}
