// @Triggers(arrayOf(
//     Trigger(
//         name = "persongroupmember_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO PersonGroupMember(groupMemberUid, groupMemberActive, groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy, groupMemberLct) 
//             VALUES (NEW.groupMemberUid, NEW.groupMemberActive, NEW.groupMemberPersonUid, NEW.groupMemberGroupUid, NEW.groupMemberMasterCsn, NEW.groupMemberLocalCsn, NEW.groupMemberLastChangedBy, NEW.groupMemberLct) 
//             /*psql ON CONFLICT (groupMemberUid) DO UPDATE 
//             SET groupMemberActive = EXCLUDED.groupMemberActive, groupMemberPersonUid = EXCLUDED.groupMemberPersonUid, groupMemberGroupUid = EXCLUDED.groupMemberGroupUid, groupMemberMasterCsn = EXCLUDED.groupMemberMasterCsn, groupMemberLocalCsn = EXCLUDED.groupMemberLocalCsn, groupMemberLastChangedBy = EXCLUDED.groupMemberLastChangedBy, groupMemberLct = EXCLUDED.groupMemberLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO PersonGroupMemberReplicate(pgmPk, pgmVersionId, pgmDestination)
//      SELECT PersonGroupMember.groupMemberUid AS pgmUid,
//             PersonGroupMember.groupMemberLct AS pgmVersionId,
//             :newNodeId AS pgmDestination
//        FROM PersonGroupMember
//       WHERE PersonGroupMember.groupMemberLct != COALESCE(
//             (SELECT pgmVersionId
//                FROM PersonGroupMemberReplicate
//               WHERE pgmPk = PersonGroupMember.groupMemberUid
//                 AND pgmDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(pgmPk, pgmDestination) DO UPDATE
//             SET pgmPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([PersonGroupMember::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO PersonGroupMemberReplicate(pgmPk, pgmVersionId, pgmDestination)
//  SELECT PersonGroupMember.groupMemberUid AS pgmUid,
//         PersonGroupMember.groupMemberLct AS pgmVersionId,
//         UserSession.usClientNodeId AS pgmDestination
//    FROM ChangeLog
//         JOIN PersonGroupMember
//             ON ChangeLog.chTableId = 44
//                AND ChangeLog.chEntityPk = PersonGroupMember.groupMemberUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND PersonGroupMember.groupMemberLct != COALESCE(
//         (SELECT pgmVersionId
//            FROM PersonGroupMemberReplicate
//           WHERE pgmPk = PersonGroupMember.groupMemberUid
//             AND pgmDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(pgmPk, pgmDestination) DO UPDATE
//     SET pgmPending = true
//  */               
// """)
// @ReplicationRunOnChange([PersonGroupMember::class])
// @ReplicationCheckPendingNotificationsFor([PersonGroupMember::class])
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
  primaryKeys = arrayOf("pgmPk", "pgmDestination"),
  indices = arrayOf(Index(value = arrayOf("pgmPk", "pgmDestination", "pgmVersionId")),
  Index(value = arrayOf("pgmDestination", "pgmPending")))

)
@Serializable
public class PersonGroupMemberReplicate {
  @ReplicationEntityForeignKey
  public var pgmPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var pgmVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var pgmDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var pgmPending: Boolean = true

  companion object {
    const val SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL = """
      PersonGroup.groupUid AS pgUid,
      PersonGroup.groupLct AS pgVersionId
    """

    const val PERSONGROUP_REPLICATE_NOT_ALREADY_UPDATE_SQL = """
      PersonGroup.groupLct != COALESCE(
         (SELECT pgVersionId
            FROM PersonGroupReplicate
           WHERE pgPk = PersonGroup.groupUid
             AND pgDestination = UserSession.usClientNodeId), 0)
    """

  }
}
