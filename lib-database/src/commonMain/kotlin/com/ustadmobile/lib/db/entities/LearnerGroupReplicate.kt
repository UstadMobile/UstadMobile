// @Triggers(arrayOf(
//     Trigger(
//         name = "learnergroup_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO LearnerGroup(learnerGroupUid, learnerGroupName, learnerGroupDescription, learnerGroupActive, learnerGroupMCSN, learnerGroupCSN, learnerGroupLCB, learnerGroupLct) 
//             VALUES (NEW.learnerGroupUid, NEW.learnerGroupName, NEW.learnerGroupDescription, NEW.learnerGroupActive, NEW.learnerGroupMCSN, NEW.learnerGroupCSN, NEW.learnerGroupLCB, NEW.learnerGroupLct) 
//             /*psql ON CONFLICT (learnerGroupUid) DO UPDATE 
//             SET learnerGroupName = EXCLUDED.learnerGroupName, learnerGroupDescription = EXCLUDED.learnerGroupDescription, learnerGroupActive = EXCLUDED.learnerGroupActive, learnerGroupMCSN = EXCLUDED.learnerGroupMCSN, learnerGroupCSN = EXCLUDED.learnerGroupCSN, learnerGroupLCB = EXCLUDED.learnerGroupLCB, learnerGroupLct = EXCLUDED.learnerGroupLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO LearnerGroupReplicate(lgPk, lgVersionId, lgDestination)
//      SELECT LearnerGroup.learnerGroupUid AS lgUid,
//             LearnerGroup.learnerGroupLct AS lgVersionId,
//             :newNodeId AS lgDestination
//        FROM LearnerGroup
//       WHERE LearnerGroup.learnerGroupLct != COALESCE(
//             (SELECT lgVersionId
//                FROM LearnerGroupReplicate
//               WHERE lgPk = LearnerGroup.learnerGroupUid
//                 AND lgDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(lgPk, lgDestination) DO UPDATE
//             SET lgPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([LearnerGroup::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO LearnerGroupReplicate(lgPk, lgVersionId, lgDestination)
//  SELECT LearnerGroup.learnerGroupUid AS lgUid,
//         LearnerGroup.learnerGroupLct AS lgVersionId,
//         UserSession.usClientNodeId AS lgDestination
//    FROM ChangeLog
//         JOIN LearnerGroup
//             ON ChangeLog.chTableId = 301
//                AND ChangeLog.chEntityPk = LearnerGroup.learnerGroupUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND LearnerGroup.learnerGroupLct != COALESCE(
//         (SELECT lgVersionId
//            FROM LearnerGroupReplicate
//           WHERE lgPk = LearnerGroup.learnerGroupUid
//             AND lgDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(lgPk, lgDestination) DO UPDATE
//     SET lgPending = true
//  */               
// """)
// @ReplicationRunOnChange([LearnerGroup::class])
// @ReplicationCheckPendingNotificationsFor([LearnerGroup::class])
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
  primaryKeys = arrayOf("lgPk", "lgDestination"),
  indices = arrayOf(Index(value = arrayOf("lgPk", "lgDestination", "lgVersionId")),
  Index(value = arrayOf("lgDestination", "lgPending")))

)
@Serializable
public class LearnerGroupReplicate {
  @ReplicationEntityForeignKey
  public var lgPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var lgVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var lgDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var lgPending: Boolean = true
}
