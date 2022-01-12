// @Triggers(arrayOf(
//     Trigger(
//         name = "grouplearningsession_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO GroupLearningSession(groupLearningSessionUid, groupLearningSessionContentUid, groupLearningSessionLearnerGroupUid, groupLearningSessionInactive, groupLearningSessionMCSN, groupLearningSessionCSN, groupLearningSessionLCB, groupLearningSessionLct) 
//             VALUES (NEW.groupLearningSessionUid, NEW.groupLearningSessionContentUid, NEW.groupLearningSessionLearnerGroupUid, NEW.groupLearningSessionInactive, NEW.groupLearningSessionMCSN, NEW.groupLearningSessionCSN, NEW.groupLearningSessionLCB, NEW.groupLearningSessionLct) 
//             /*psql ON CONFLICT (groupLearningSessionUid) DO UPDATE 
//             SET groupLearningSessionContentUid = EXCLUDED.groupLearningSessionContentUid, groupLearningSessionLearnerGroupUid = EXCLUDED.groupLearningSessionLearnerGroupUid, groupLearningSessionInactive = EXCLUDED.groupLearningSessionInactive, groupLearningSessionMCSN = EXCLUDED.groupLearningSessionMCSN, groupLearningSessionCSN = EXCLUDED.groupLearningSessionCSN, groupLearningSessionLCB = EXCLUDED.groupLearningSessionLCB, groupLearningSessionLct = EXCLUDED.groupLearningSessionLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO GroupLearningSessionReplicate(glsPk, glsVersionId, glsDestination)
//      SELECT GroupLearningSession.groupLearningSessionUid AS glsUid,
//             GroupLearningSession.groupLearningSessionLct AS glsVersionId,
//             :newNodeId AS glsDestination
//        FROM GroupLearningSession
//       WHERE GroupLearningSession.groupLearningSessionLct != COALESCE(
//             (SELECT glsVersionId
//                FROM GroupLearningSessionReplicate
//               WHERE glsPk = GroupLearningSession.groupLearningSessionUid
//                 AND glsDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(glsPk, glsDestination) DO UPDATE
//             SET glsPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([GroupLearningSession::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO GroupLearningSessionReplicate(glsPk, glsVersionId, glsDestination)
//  SELECT GroupLearningSession.groupLearningSessionUid AS glsUid,
//         GroupLearningSession.groupLearningSessionLct AS glsVersionId,
//         UserSession.usClientNodeId AS glsDestination
//    FROM ChangeLog
//         JOIN GroupLearningSession
//             ON ChangeLog.chTableId = 302
//                AND ChangeLog.chEntityPk = GroupLearningSession.groupLearningSessionUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND GroupLearningSession.groupLearningSessionLct != COALESCE(
//         (SELECT glsVersionId
//            FROM GroupLearningSessionReplicate
//           WHERE glsPk = GroupLearningSession.groupLearningSessionUid
//             AND glsDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(glsPk, glsDestination) DO UPDATE
//     SET glsPending = true
//  */               
// """)
// @ReplicationRunOnChange([GroupLearningSession::class])
// @ReplicationCheckPendingNotificationsFor([GroupLearningSession::class])
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
  primaryKeys = arrayOf("glsPk", "glsDestination"),
  indices = arrayOf(Index(value = arrayOf("glsPk", "glsDestination", "glsVersionId")),
  Index(value = arrayOf("glsDestination", "glsPending")))

)
@Serializable
public class GroupLearningSessionReplicate {
  @ReplicationEntityForeignKey
  public var glsPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var glsVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var glsDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var glsPending: Boolean = true
}
