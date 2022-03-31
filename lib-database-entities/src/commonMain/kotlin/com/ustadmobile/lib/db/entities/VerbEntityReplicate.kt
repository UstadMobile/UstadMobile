// @Triggers(arrayOf(
//     Trigger(
//         name = "verbentity_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO VerbEntity(verbUid, urlId, verbInActive, verbMasterChangeSeqNum, verbLocalChangeSeqNum, verbLastChangedBy, verbLct) 
//             VALUES (NEW.verbUid, NEW.urlId, NEW.verbInActive, NEW.verbMasterChangeSeqNum, NEW.verbLocalChangeSeqNum, NEW.verbLastChangedBy, NEW.verbLct) 
//             /*psql ON CONFLICT (verbUid) DO UPDATE 
//             SET urlId = EXCLUDED.urlId, verbInActive = EXCLUDED.verbInActive, verbMasterChangeSeqNum = EXCLUDED.verbMasterChangeSeqNum, verbLocalChangeSeqNum = EXCLUDED.verbLocalChangeSeqNum, verbLastChangedBy = EXCLUDED.verbLastChangedBy, verbLct = EXCLUDED.verbLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO VerbEntityReplicate(vePk, veVersionId, veDestination)
//      SELECT VerbEntity.verbUid AS veUid,
//             VerbEntity.verbLct AS veVersionId,
//             :newNodeId AS veDestination
//        FROM VerbEntity
//       WHERE VerbEntity.verbLct != COALESCE(
//             (SELECT veVersionId
//                FROM VerbEntityReplicate
//               WHERE vePk = VerbEntity.verbUid
//                 AND veDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(vePk, veDestination) DO UPDATE
//             SET vePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([VerbEntity::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO VerbEntityReplicate(vePk, veVersionId, veDestination)
//  SELECT VerbEntity.verbUid AS veUid,
//         VerbEntity.verbLct AS veVersionId,
//         UserSession.usClientNodeId AS veDestination
//    FROM ChangeLog
//         JOIN VerbEntity
//             ON ChangeLog.chTableId = 62
//                AND ChangeLog.chEntityPk = VerbEntity.verbUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND VerbEntity.verbLct != COALESCE(
//         (SELECT veVersionId
//            FROM VerbEntityReplicate
//           WHERE vePk = VerbEntity.verbUid
//             AND veDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(vePk, veDestination) DO UPDATE
//     SET vePending = true
//  */               
// """)
// @ReplicationRunOnChange([VerbEntity::class])
// @ReplicationCheckPendingNotificationsFor([VerbEntity::class])
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
  primaryKeys = arrayOf("vePk", "veDestination"),
  indices = arrayOf(Index(value = arrayOf("vePk", "veDestination", "veVersionId")),
  Index(value = arrayOf("veDestination", "vePending")))

)
@Serializable
public class VerbEntityReplicate {
  @ReplicationEntityForeignKey
  public var vePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var veVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var veDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var vePending: Boolean = true
}
