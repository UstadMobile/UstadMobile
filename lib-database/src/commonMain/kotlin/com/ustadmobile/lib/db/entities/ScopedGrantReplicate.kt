// @Triggers(arrayOf(
//     Trigger(
//         name = "scopedgrant_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ScopedGrant(sgUid, sgPcsn, sgLcsn, sgLcb, sgLct, sgTableId, sgEntityUid, sgPermissions, sgGroupUid, sgIndex, sgFlags) 
//             VALUES (NEW.sgUid, NEW.sgPcsn, NEW.sgLcsn, NEW.sgLcb, NEW.sgLct, NEW.sgTableId, NEW.sgEntityUid, NEW.sgPermissions, NEW.sgGroupUid, NEW.sgIndex, NEW.sgFlags) 
//             /*psql ON CONFLICT (sgUid) DO UPDATE 
//             SET sgPcsn = EXCLUDED.sgPcsn, sgLcsn = EXCLUDED.sgLcsn, sgLcb = EXCLUDED.sgLcb, sgLct = EXCLUDED.sgLct, sgTableId = EXCLUDED.sgTableId, sgEntityUid = EXCLUDED.sgEntityUid, sgPermissions = EXCLUDED.sgPermissions, sgGroupUid = EXCLUDED.sgGroupUid, sgIndex = EXCLUDED.sgIndex, sgFlags = EXCLUDED.sgFlags
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ScopedGrantReplicate(sgPk, sgVersionId, sgDestination)
//      SELECT ScopedGrant.sgUid AS sgUid,
//             ScopedGrant.sgLct AS sgVersionId,
//             :newNodeId AS sgDestination
//        FROM ScopedGrant
//       WHERE ScopedGrant.sgLct != COALESCE(
//             (SELECT sgVersionId
//                FROM ScopedGrantReplicate
//               WHERE sgPk = ScopedGrant.sgUid
//                 AND sgDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(sgPk, sgDestination) DO UPDATE
//             SET sgPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ScopedGrant::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ScopedGrantReplicate(sgPk, sgVersionId, sgDestination)
//  SELECT ScopedGrant.sgUid AS sgUid,
//         ScopedGrant.sgLct AS sgVersionId,
//         UserSession.usClientNodeId AS sgDestination
//    FROM ChangeLog
//         JOIN ScopedGrant
//             ON ChangeLog.chTableId = 48
//                AND ChangeLog.chEntityPk = ScopedGrant.sgUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ScopedGrant.sgLct != COALESCE(
//         (SELECT sgVersionId
//            FROM ScopedGrantReplicate
//           WHERE sgPk = ScopedGrant.sgUid
//             AND sgDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(sgPk, sgDestination) DO UPDATE
//     SET sgPending = true
//  */               
// """)
// @ReplicationRunOnChange([ScopedGrant::class])
// @ReplicationCheckPendingNotificationsFor([ScopedGrant::class])
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
  primaryKeys = arrayOf("sgPk", "sgDestination"),
  indices = arrayOf(Index(value = arrayOf("sgPk", "sgDestination", "sgVersionId")),
  Index(value = arrayOf("sgDestination", "sgPending")))

)
@Serializable
public class ScopedGrantReplicate {
  @ReplicationEntityForeignKey
  public var sgPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var sgVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var sgDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var sgPending: Boolean = true
}
