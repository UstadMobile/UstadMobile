// @Triggers(arrayOf(
//     Trigger(
//         name = "usersession_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO UserSession(usUid, usPcsn, usLcsn, usLcb, usLct, usPersonUid, usClientNodeId, usStartTime, usEndTime, usStatus, usReason, usAuth, usSessionType) 
//             VALUES (NEW.usUid, NEW.usPcsn, NEW.usLcsn, NEW.usLcb, NEW.usLct, NEW.usPersonUid, NEW.usClientNodeId, NEW.usStartTime, NEW.usEndTime, NEW.usStatus, NEW.usReason, NEW.usAuth, NEW.usSessionType) 
//             /*psql ON CONFLICT (usUid) DO UPDATE 
//             SET usPcsn = EXCLUDED.usPcsn, usLcsn = EXCLUDED.usLcsn, usLcb = EXCLUDED.usLcb, usLct = EXCLUDED.usLct, usPersonUid = EXCLUDED.usPersonUid, usClientNodeId = EXCLUDED.usClientNodeId, usStartTime = EXCLUDED.usStartTime, usEndTime = EXCLUDED.usEndTime, usStatus = EXCLUDED.usStatus, usReason = EXCLUDED.usReason, usAuth = EXCLUDED.usAuth, usSessionType = EXCLUDED.usSessionType
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO UserSessionReplicate(usPk, usVersionId, usDestination)
//      SELECT UserSession.usUid AS usUid,
//             UserSession.usLct AS usVersionId,
//             :newNodeId AS usDestination
//        FROM UserSession
//       WHERE UserSession.usLct != COALESCE(
//             (SELECT usVersionId
//                FROM UserSessionReplicate
//               WHERE usPk = UserSession.usUid
//                 AND usDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(usPk, usDestination) DO UPDATE
//             SET usPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([UserSession::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO UserSessionReplicate(usPk, usVersionId, usDestination)
//  SELECT UserSession.usUid AS usUid,
//         UserSession.usLct AS usVersionId,
//         UserSession.usClientNodeId AS usDestination
//    FROM ChangeLog
//         JOIN UserSession
//             ON ChangeLog.chTableId = 679
//                AND ChangeLog.chEntityPk = UserSession.usUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND UserSession.usLct != COALESCE(
//         (SELECT usVersionId
//            FROM UserSessionReplicate
//           WHERE usPk = UserSession.usUid
//             AND usDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(usPk, usDestination) DO UPDATE
//     SET usPending = true
//  */               
// """)
// @ReplicationRunOnChange([UserSession::class])
// @ReplicationCheckPendingNotificationsFor([UserSession::class])
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
  primaryKeys = arrayOf("usPk", "usDestination"),
  indices = arrayOf(Index(value = arrayOf("usPk", "usDestination", "usVersionId")),
  Index(value = arrayOf("usDestination", "usPending")))

)
@Serializable
public class UserSessionReplicate {
  @ReplicationEntityForeignKey
  public var usPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var usVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var usDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var usPending: Boolean = true
}
