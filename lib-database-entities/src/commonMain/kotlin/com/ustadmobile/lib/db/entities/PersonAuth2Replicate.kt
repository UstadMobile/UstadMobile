// @Triggers(arrayOf(
//     Trigger(
//         name = "personauth2_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO PersonAuth2(pauthUid, pauthMechanism, pauthAuth, pauthLcsn, pauthPcsn, pauthLcb, pauthLct) 
//             VALUES (NEW.pauthUid, NEW.pauthMechanism, NEW.pauthAuth, NEW.pauthLcsn, NEW.pauthPcsn, NEW.pauthLcb, NEW.pauthLct) 
//             /*psql ON CONFLICT (pauthUid) DO UPDATE 
//             SET pauthMechanism = EXCLUDED.pauthMechanism, pauthAuth = EXCLUDED.pauthAuth, pauthLcsn = EXCLUDED.pauthLcsn, pauthPcsn = EXCLUDED.pauthPcsn, pauthLcb = EXCLUDED.pauthLcb, pauthLct = EXCLUDED.pauthLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO PersonAuth2Replicate(paPk, paVersionId, paDestination)
//      SELECT PersonAuth2.pauthUid AS paUid,
//             PersonAuth2.pauthLct AS paVersionId,
//             :newNodeId AS paDestination
//        FROM PersonAuth2
//       WHERE PersonAuth2.pauthLct != COALESCE(
//             (SELECT paVersionId
//                FROM PersonAuth2Replicate
//               WHERE paPk = PersonAuth2.pauthUid
//                 AND paDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(paPk, paDestination) DO UPDATE
//             SET paPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([PersonAuth2::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO PersonAuth2Replicate(paPk, paVersionId, paDestination)
//  SELECT PersonAuth2.pauthUid AS paUid,
//         PersonAuth2.pauthLct AS paVersionId,
//         UserSession.usClientNodeId AS paDestination
//    FROM ChangeLog
//         JOIN PersonAuth2
//             ON ChangeLog.chTableId = 678
//                AND ChangeLog.chEntityPk = PersonAuth2.pauthUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND PersonAuth2.pauthLct != COALESCE(
//         (SELECT paVersionId
//            FROM PersonAuth2Replicate
//           WHERE paPk = PersonAuth2.pauthUid
//             AND paDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(paPk, paDestination) DO UPDATE
//     SET paPending = true
//  */               
// """)
// @ReplicationRunOnChange([PersonAuth2::class])
// @ReplicationCheckPendingNotificationsFor([PersonAuth2::class])
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
  primaryKeys = arrayOf("paPk", "paDestination"),
  indices = arrayOf(Index(value = arrayOf("paPk", "paDestination", "paVersionId")),
  Index(value = arrayOf("paDestination", "paPending")))

)
@Serializable
public class PersonAuth2Replicate {
  @ReplicationEntityForeignKey
  public var paPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var paVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var paDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var paPending: Boolean = true
}
