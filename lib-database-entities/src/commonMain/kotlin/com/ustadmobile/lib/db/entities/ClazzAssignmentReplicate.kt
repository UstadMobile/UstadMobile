// @Triggers(arrayOf(
//     Trigger(
//         name = "clazzassignment_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ClazzAssignment(caUid, caTitle, caDescription, caDeadlineDate, caStartDate, caLateSubmissionType, caLateSubmissionPenalty, caGracePeriodDate, caActive, caClassCommentEnabled, caPrivateCommentsEnabled, caClazzUid, caLocalChangeSeqNum, caMasterChangeSeqNum, caLastChangedBy, caLct) 
//             VALUES (NEW.caUid, NEW.caTitle, NEW.caDescription, NEW.caDeadlineDate, NEW.caStartDate, NEW.caLateSubmissionType, NEW.caLateSubmissionPenalty, NEW.caGracePeriodDate, NEW.caActive, NEW.caClassCommentEnabled, NEW.caPrivateCommentsEnabled, NEW.caClazzUid, NEW.caLocalChangeSeqNum, NEW.caMasterChangeSeqNum, NEW.caLastChangedBy, NEW.caLct) 
//             /*psql ON CONFLICT (caUid) DO UPDATE 
//             SET caTitle = EXCLUDED.caTitle, caDescription = EXCLUDED.caDescription, caDeadlineDate = EXCLUDED.caDeadlineDate, caStartDate = EXCLUDED.caStartDate, caLateSubmissionType = EXCLUDED.caLateSubmissionType, caLateSubmissionPenalty = EXCLUDED.caLateSubmissionPenalty, caGracePeriodDate = EXCLUDED.caGracePeriodDate, caActive = EXCLUDED.caActive, caClassCommentEnabled = EXCLUDED.caClassCommentEnabled, caPrivateCommentsEnabled = EXCLUDED.caPrivateCommentsEnabled, caClazzUid = EXCLUDED.caClazzUid, caLocalChangeSeqNum = EXCLUDED.caLocalChangeSeqNum, caMasterChangeSeqNum = EXCLUDED.caMasterChangeSeqNum, caLastChangedBy = EXCLUDED.caLastChangedBy, caLct = EXCLUDED.caLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ClazzAssignmentReplicate(caPk, caVersionId, caDestination)
//      SELECT ClazzAssignment.caUid AS caUid,
//             ClazzAssignment.caLct AS caVersionId,
//             :newNodeId AS caDestination
//        FROM ClazzAssignment
//       WHERE ClazzAssignment.caLct != COALESCE(
//             (SELECT caVersionId
//                FROM ClazzAssignmentReplicate
//               WHERE caPk = ClazzAssignment.caUid
//                 AND caDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(caPk, caDestination) DO UPDATE
//             SET caPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ClazzAssignment::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ClazzAssignmentReplicate(caPk, caVersionId, caDestination)
//  SELECT ClazzAssignment.caUid AS caUid,
//         ClazzAssignment.caLct AS caVersionId,
//         UserSession.usClientNodeId AS caDestination
//    FROM ChangeLog
//         JOIN ClazzAssignment
//             ON ChangeLog.chTableId = 520
//                AND ChangeLog.chEntityPk = ClazzAssignment.caUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ClazzAssignment.caLct != COALESCE(
//         (SELECT caVersionId
//            FROM ClazzAssignmentReplicate
//           WHERE caPk = ClazzAssignment.caUid
//             AND caDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(caPk, caDestination) DO UPDATE
//     SET caPending = true
//  */               
// """)
// @ReplicationRunOnChange([ClazzAssignment::class])
// @ReplicationCheckPendingNotificationsFor([ClazzAssignment::class])
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
  primaryKeys = arrayOf("caPk", "caDestination"),
  indices = arrayOf(Index(value = arrayOf("caPk", "caDestination", "caVersionId")),
  Index(value = arrayOf("caDestination", "caPending")))

)
@Serializable
public class ClazzAssignmentReplicate {
  @ReplicationEntityForeignKey
  public var caPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var caVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var caDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var caPending: Boolean = true
}
