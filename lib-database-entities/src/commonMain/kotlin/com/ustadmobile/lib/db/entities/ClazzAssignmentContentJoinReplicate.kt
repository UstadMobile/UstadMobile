// @Triggers(arrayOf(
//     Trigger(
//         name = "clazzassignmentcontentjoin_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ClazzAssignmentContentJoin(cacjUid, cacjContentUid, cacjAssignmentUid, cacjActive, cacjMCSN, cacjLCSN, cacjLCB, cacjLct) 
//             VALUES (NEW.cacjUid, NEW.cacjContentUid, NEW.cacjAssignmentUid, NEW.cacjActive, NEW.cacjMCSN, NEW.cacjLCSN, NEW.cacjLCB, NEW.cacjLct) 
//             /*psql ON CONFLICT (cacjUid) DO UPDATE 
//             SET cacjContentUid = EXCLUDED.cacjContentUid, cacjAssignmentUid = EXCLUDED.cacjAssignmentUid, cacjActive = EXCLUDED.cacjActive, cacjMCSN = EXCLUDED.cacjMCSN, cacjLCSN = EXCLUDED.cacjLCSN, cacjLCB = EXCLUDED.cacjLCB, cacjLct = EXCLUDED.cacjLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ClazzAssignmentContentJoinReplicate(cacjPk, cacjVersionId, cacjDestination)
//      SELECT ClazzAssignmentContentJoin.cacjUid AS cacjUid,
//             ClazzAssignmentContentJoin.cacjLct AS cacjVersionId,
//             :newNodeId AS cacjDestination
//        FROM ClazzAssignmentContentJoin
//       WHERE ClazzAssignmentContentJoin.cacjLct != COALESCE(
//             (SELECT cacjVersionId
//                FROM ClazzAssignmentContentJoinReplicate
//               WHERE cacjPk = ClazzAssignmentContentJoin.cacjUid
//                 AND cacjDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(cacjPk, cacjDestination) DO UPDATE
//             SET cacjPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ClazzAssignmentContentJoin::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ClazzAssignmentContentJoinReplicate(cacjPk, cacjVersionId, cacjDestination)
//  SELECT ClazzAssignmentContentJoin.cacjUid AS cacjUid,
//         ClazzAssignmentContentJoin.cacjLct AS cacjVersionId,
//         UserSession.usClientNodeId AS cacjDestination
//    FROM ChangeLog
//         JOIN ClazzAssignmentContentJoin
//             ON ChangeLog.chTableId = 521
//                AND ChangeLog.chEntityPk = ClazzAssignmentContentJoin.cacjUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ClazzAssignmentContentJoin.cacjLct != COALESCE(
//         (SELECT cacjVersionId
//            FROM ClazzAssignmentContentJoinReplicate
//           WHERE cacjPk = ClazzAssignmentContentJoin.cacjUid
//             AND cacjDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(cacjPk, cacjDestination) DO UPDATE
//     SET cacjPending = true
//  */               
// """)
// @ReplicationRunOnChange([ClazzAssignmentContentJoin::class])
// @ReplicationCheckPendingNotificationsFor([ClazzAssignmentContentJoin::class])
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
  primaryKeys = arrayOf("cacjPk", "cacjDestination"),
  indices = arrayOf(Index(value = arrayOf("cacjPk", "cacjDestination", "cacjVersionId")),
  Index(value = arrayOf("cacjDestination", "cacjPending")))

)
@Serializable
public class ClazzAssignmentContentJoinReplicate {
  @ReplicationEntityForeignKey
  public var cacjPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var cacjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cacjDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var cacjPending: Boolean = true
}
