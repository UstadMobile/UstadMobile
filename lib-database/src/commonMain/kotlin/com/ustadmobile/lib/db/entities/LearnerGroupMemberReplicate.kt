// @Triggers(arrayOf(
//     Trigger(
//         name = "learnergroupmember_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO LearnerGroupMember(learnerGroupMemberUid, learnerGroupMemberPersonUid, learnerGroupMemberLgUid, learnerGroupMemberRole, learnerGroupMemberActive, learnerGroupMemberMCSN, learnerGroupMemberCSN, learnerGroupMemberLCB, learnerGroupMemberLct) 
//             VALUES (NEW.learnerGroupMemberUid, NEW.learnerGroupMemberPersonUid, NEW.learnerGroupMemberLgUid, NEW.learnerGroupMemberRole, NEW.learnerGroupMemberActive, NEW.learnerGroupMemberMCSN, NEW.learnerGroupMemberCSN, NEW.learnerGroupMemberLCB, NEW.learnerGroupMemberLct) 
//             /*psql ON CONFLICT (learnerGroupMemberUid) DO UPDATE 
//             SET learnerGroupMemberPersonUid = EXCLUDED.learnerGroupMemberPersonUid, learnerGroupMemberLgUid = EXCLUDED.learnerGroupMemberLgUid, learnerGroupMemberRole = EXCLUDED.learnerGroupMemberRole, learnerGroupMemberActive = EXCLUDED.learnerGroupMemberActive, learnerGroupMemberMCSN = EXCLUDED.learnerGroupMemberMCSN, learnerGroupMemberCSN = EXCLUDED.learnerGroupMemberCSN, learnerGroupMemberLCB = EXCLUDED.learnerGroupMemberLCB, learnerGroupMemberLct = EXCLUDED.learnerGroupMemberLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO LearnerGroupMemberReplicate(lgmPk, lgmVersionId, lgmDestination)
//      SELECT LearnerGroupMember.learnerGroupMemberUid AS lgmUid,
//             LearnerGroupMember.learnerGroupMemberLct AS lgmVersionId,
//             :newNodeId AS lgmDestination
//        FROM LearnerGroupMember
//       WHERE LearnerGroupMember.learnerGroupMemberLct != COALESCE(
//             (SELECT lgmVersionId
//                FROM LearnerGroupMemberReplicate
//               WHERE lgmPk = LearnerGroupMember.learnerGroupMemberUid
//                 AND lgmDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(lgmPk, lgmDestination) DO UPDATE
//             SET lgmPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([LearnerGroupMember::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO LearnerGroupMemberReplicate(lgmPk, lgmVersionId, lgmDestination)
//  SELECT LearnerGroupMember.learnerGroupMemberUid AS lgmUid,
//         LearnerGroupMember.learnerGroupMemberLct AS lgmVersionId,
//         UserSession.usClientNodeId AS lgmDestination
//    FROM ChangeLog
//         JOIN LearnerGroupMember
//             ON ChangeLog.chTableId = 300
//                AND ChangeLog.chEntityPk = LearnerGroupMember.learnerGroupMemberUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND LearnerGroupMember.learnerGroupMemberLct != COALESCE(
//         (SELECT lgmVersionId
//            FROM LearnerGroupMemberReplicate
//           WHERE lgmPk = LearnerGroupMember.learnerGroupMemberUid
//             AND lgmDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(lgmPk, lgmDestination) DO UPDATE
//     SET lgmPending = true
//  */               
// """)
// @ReplicationRunOnChange([LearnerGroupMember::class])
// @ReplicationCheckPendingNotificationsFor([LearnerGroupMember::class])
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
  primaryKeys = arrayOf("lgmPk", "lgmDestination"),
  indices = arrayOf(Index(value = arrayOf("lgmPk", "lgmDestination", "lgmVersionId")),
  Index(value = arrayOf("lgmDestination", "lgmPending")))

)
@Serializable
public class LearnerGroupMemberReplicate {
  @ReplicationEntityForeignKey
  public var lgmPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var lgmVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var lgmDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var lgmPending: Boolean = true
}
