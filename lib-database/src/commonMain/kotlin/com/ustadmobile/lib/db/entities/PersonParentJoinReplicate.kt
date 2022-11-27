// @Triggers(arrayOf(
//     Trigger(
//         name = "personparentjoin_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO PersonParentJoin(ppjUid, ppjPcsn, ppjLcsn, ppjLcb, ppjLct, ppjParentPersonUid, ppjMinorPersonUid, ppjRelationship, ppjEmail, ppjPhone, ppjInactive, ppjStatus, ppjApprovalTiemstamp, ppjApprovalIpAddr) 
//             VALUES (NEW.ppjUid, NEW.ppjPcsn, NEW.ppjLcsn, NEW.ppjLcb, NEW.ppjLct, NEW.ppjParentPersonUid, NEW.ppjMinorPersonUid, NEW.ppjRelationship, NEW.ppjEmail, NEW.ppjPhone, NEW.ppjInactive, NEW.ppjStatus, NEW.ppjApprovalTiemstamp, NEW.ppjApprovalIpAddr) 
//             /*psql ON CONFLICT (ppjUid) DO UPDATE 
//             SET ppjPcsn = EXCLUDED.ppjPcsn, ppjLcsn = EXCLUDED.ppjLcsn, ppjLcb = EXCLUDED.ppjLcb, ppjLct = EXCLUDED.ppjLct, ppjParentPersonUid = EXCLUDED.ppjParentPersonUid, ppjMinorPersonUid = EXCLUDED.ppjMinorPersonUid, ppjRelationship = EXCLUDED.ppjRelationship, ppjEmail = EXCLUDED.ppjEmail, ppjPhone = EXCLUDED.ppjPhone, ppjInactive = EXCLUDED.ppjInactive, ppjStatus = EXCLUDED.ppjStatus, ppjApprovalTiemstamp = EXCLUDED.ppjApprovalTiemstamp, ppjApprovalIpAddr = EXCLUDED.ppjApprovalIpAddr
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO PersonParentJoinReplicate(ppjPk, ppjVersionId, ppjDestination)
//      SELECT PersonParentJoin.ppjUid AS ppjUid,
//             PersonParentJoin.ppjLct AS ppjVersionId,
//             :newNodeId AS ppjDestination
//        FROM PersonParentJoin
//       WHERE PersonParentJoin.ppjLct != COALESCE(
//             (SELECT ppjVersionId
//                FROM PersonParentJoinReplicate
//               WHERE ppjPk = PersonParentJoin.ppjUid
//                 AND ppjDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(ppjPk, ppjDestination) DO UPDATE
//             SET ppjPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([PersonParentJoin::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO PersonParentJoinReplicate(ppjPk, ppjVersionId, ppjDestination)
//  SELECT PersonParentJoin.ppjUid AS ppjUid,
//         PersonParentJoin.ppjLct AS ppjVersionId,
//         UserSession.usClientNodeId AS ppjDestination
//    FROM ChangeLog
//         JOIN PersonParentJoin
//             ON ChangeLog.chTableId = 512
//                AND ChangeLog.chEntityPk = PersonParentJoin.ppjUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND PersonParentJoin.ppjLct != COALESCE(
//         (SELECT ppjVersionId
//            FROM PersonParentJoinReplicate
//           WHERE ppjPk = PersonParentJoin.ppjUid
//             AND ppjDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(ppjPk, ppjDestination) DO UPDATE
//     SET ppjPending = true
//  */               
// """)
// @ReplicationRunOnChange([PersonParentJoin::class])
// @ReplicationCheckPendingNotificationsFor([PersonParentJoin::class])
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
  primaryKeys = arrayOf("ppjPk", "ppjDestination"),
  indices = arrayOf(Index(value = arrayOf("ppjPk", "ppjDestination", "ppjVersionId")),
  Index(value = arrayOf("ppjDestination", "ppjPending")))

)
@Serializable
public class PersonParentJoinReplicate {
  @ReplicationEntityForeignKey
  public var ppjPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var ppjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ppjDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var ppjPending: Boolean = true
}
