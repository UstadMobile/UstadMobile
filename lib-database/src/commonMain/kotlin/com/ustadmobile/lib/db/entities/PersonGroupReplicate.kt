// @Triggers(arrayOf(
//     Trigger(
//         name = "persongroup_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO PersonGroup(groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupLct, groupName, groupActive, personGroupFlag) 
//             VALUES (NEW.groupUid, NEW.groupMasterCsn, NEW.groupLocalCsn, NEW.groupLastChangedBy, NEW.groupLct, NEW.groupName, NEW.groupActive, NEW.personGroupFlag) 
//             /*psql ON CONFLICT (groupUid) DO UPDATE 
//             SET groupMasterCsn = EXCLUDED.groupMasterCsn, groupLocalCsn = EXCLUDED.groupLocalCsn, groupLastChangedBy = EXCLUDED.groupLastChangedBy, groupLct = EXCLUDED.groupLct, groupName = EXCLUDED.groupName, groupActive = EXCLUDED.groupActive, personGroupFlag = EXCLUDED.personGroupFlag
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO PersonGroupReplicate(pgPk, pgVersionId, pgDestination)
//      SELECT PersonGroup.groupUid AS pgUid,
//             PersonGroup.groupLct AS pgVersionId,
//             :newNodeId AS pgDestination
//        FROM PersonGroup
//       WHERE PersonGroup.groupLct != COALESCE(
//             (SELECT pgVersionId
//                FROM PersonGroupReplicate
//               WHERE pgPk = PersonGroup.groupUid
//                 AND pgDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(pgPk, pgDestination) DO UPDATE
//             SET pgPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO PersonGroupReplicate(pgPk, pgVersionId, pgDestination)
//  SELECT PersonGroup.groupUid AS pgUid,
//         PersonGroup.groupLct AS pgVersionId,
//         UserSession.usClientNodeId AS pgDestination
//    FROM ChangeLog
//         JOIN PersonGroup
//             ON ChangeLog.chTableId = 43
//                AND ChangeLog.chEntityPk = PersonGroup.groupUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND PersonGroup.groupLct != COALESCE(
//         (SELECT pgVersionId
//            FROM PersonGroupReplicate
//           WHERE pgPk = PersonGroup.groupUid
//             AND pgDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(pgPk, pgDestination) DO UPDATE
//     SET pgPending = true
//  */               
// """)
// @ReplicationRunOnChange([PersonGroup::class])
// @ReplicationCheckPendingNotificationsFor([PersonGroup::class])
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
  primaryKeys = arrayOf("pgPk", "pgDestination"),
  indices = arrayOf(Index(value = arrayOf("pgPk", "pgDestination", "pgVersionId")),
  Index(value = arrayOf("pgDestination", "pgPending")))

)
@Serializable
public class PersonGroupReplicate {
  @ReplicationEntityForeignKey
  public var pgPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var pgVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var pgDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var pgPending: Boolean = true

  companion object {
    const val SELECT_PERSONGROUP_REPLICATE_FIELDS_SQL = """
      PersonGroup.groupUid AS pgUid
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
