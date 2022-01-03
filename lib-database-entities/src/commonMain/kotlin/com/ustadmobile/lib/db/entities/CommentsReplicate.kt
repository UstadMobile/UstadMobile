// @Triggers(arrayOf(
//     Trigger(
//         name = "comments_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO Comments(commentsUid, commentsText, commentsEntityType, commentsEntityUid, commentsPublic, commentsStatus, commentsPersonUid, commentsToPersonUid, commentsFlagged, commentsInActive, commentsDateTimeAdded, commentsDateTimeUpdated, commentsMCSN, commentsLCSN, commentsLCB, commentsLct) 
//             VALUES (NEW.commentsUid, NEW.commentsText, NEW.commentsEntityType, NEW.commentsEntityUid, NEW.commentsPublic, NEW.commentsStatus, NEW.commentsPersonUid, NEW.commentsToPersonUid, NEW.commentsFlagged, NEW.commentsInActive, NEW.commentsDateTimeAdded, NEW.commentsDateTimeUpdated, NEW.commentsMCSN, NEW.commentsLCSN, NEW.commentsLCB, NEW.commentsLct) 
//             /*psql ON CONFLICT (commentsUid) DO UPDATE 
//             SET commentsText = EXCLUDED.commentsText, commentsEntityType = EXCLUDED.commentsEntityType, commentsEntityUid = EXCLUDED.commentsEntityUid, commentsPublic = EXCLUDED.commentsPublic, commentsStatus = EXCLUDED.commentsStatus, commentsPersonUid = EXCLUDED.commentsPersonUid, commentsToPersonUid = EXCLUDED.commentsToPersonUid, commentsFlagged = EXCLUDED.commentsFlagged, commentsInActive = EXCLUDED.commentsInActive, commentsDateTimeAdded = EXCLUDED.commentsDateTimeAdded, commentsDateTimeUpdated = EXCLUDED.commentsDateTimeUpdated, commentsMCSN = EXCLUDED.commentsMCSN, commentsLCSN = EXCLUDED.commentsLCSN, commentsLCB = EXCLUDED.commentsLCB, commentsLct = EXCLUDED.commentsLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO CommentsReplicate(commentsPk, commentsVersionId, commentsDestination)
//      SELECT Comments.commentsUid AS commentsUid,
//             Comments.commentsLct AS commentsVersionId,
//             :newNodeId AS commentsDestination
//        FROM Comments
//       WHERE Comments.commentsLct != COALESCE(
//             (SELECT commentsVersionId
//                FROM CommentsReplicate
//               WHERE commentsPk = Comments.commentsUid
//                 AND commentsDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(commentsPk, commentsDestination) DO UPDATE
//             SET commentsPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([Comments::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO CommentsReplicate(commentsPk, commentsVersionId, commentsDestination)
//  SELECT Comments.commentsUid AS commentsUid,
//         Comments.commentsLct AS commentsVersionId,
//         UserSession.usClientNodeId AS commentsDestination
//    FROM ChangeLog
//         JOIN Comments
//             ON ChangeLog.chTableId = 208
//                AND ChangeLog.chEntityPk = Comments.commentsUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND Comments.commentsLct != COALESCE(
//         (SELECT commentsVersionId
//            FROM CommentsReplicate
//           WHERE commentsPk = Comments.commentsUid
//             AND commentsDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(commentsPk, commentsDestination) DO UPDATE
//     SET commentsPending = true
//  */               
// """)
// @ReplicationRunOnChange([Comments::class])
// @ReplicationCheckPendingNotificationsFor([Comments::class])
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
  primaryKeys = arrayOf("commentsPk", "commentsDestination"),
  indices = arrayOf(Index(value = arrayOf("commentsPk", "commentsDestination",
      "commentsVersionId")),
  Index(value = arrayOf("commentsDestination", "commentsPending")))

)
@Serializable
public class CommentsReplicate {
  @ReplicationEntityForeignKey
  public var commentsPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var commentsVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var commentsDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var commentsPending: Boolean = true
}
