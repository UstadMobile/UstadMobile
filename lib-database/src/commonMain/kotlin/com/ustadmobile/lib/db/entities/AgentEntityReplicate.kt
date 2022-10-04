// @Triggers(arrayOf(
//     Trigger(
//         name = "agententity_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO AgentEntity(agentUid, agentMbox, agentMbox_sha1sum, agentOpenid, agentAccountName, agentHomePage, agentPersonUid, statementMasterChangeSeqNum, statementLocalChangeSeqNum, statementLastChangedBy, agentLct) 
//             VALUES (NEW.agentUid, NEW.agentMbox, NEW.agentMbox_sha1sum, NEW.agentOpenid, NEW.agentAccountName, NEW.agentHomePage, NEW.agentPersonUid, NEW.statementMasterChangeSeqNum, NEW.statementLocalChangeSeqNum, NEW.statementLastChangedBy, NEW.agentLct) 
//             /*psql ON CONFLICT (agentUid) DO UPDATE 
//             SET agentMbox = EXCLUDED.agentMbox, agentMbox_sha1sum = EXCLUDED.agentMbox_sha1sum, agentOpenid = EXCLUDED.agentOpenid, agentAccountName = EXCLUDED.agentAccountName, agentHomePage = EXCLUDED.agentHomePage, agentPersonUid = EXCLUDED.agentPersonUid, statementMasterChangeSeqNum = EXCLUDED.statementMasterChangeSeqNum, statementLocalChangeSeqNum = EXCLUDED.statementLocalChangeSeqNum, statementLastChangedBy = EXCLUDED.statementLastChangedBy, agentLct = EXCLUDED.agentLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO AgentEntityReplicate(aePk, aeVersionId, aeDestination)
//      SELECT AgentEntity.agentUid AS aeUid,
//             AgentEntity.agentLct AS aeVersionId,
//             :newNodeId AS aeDestination
//        FROM AgentEntity
//       WHERE AgentEntity.agentLct != COALESCE(
//             (SELECT aeVersionId
//                FROM AgentEntityReplicate
//               WHERE aePk = AgentEntity.agentUid
//                 AND aeDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(aePk, aeDestination) DO UPDATE
//             SET aePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([AgentEntity::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO AgentEntityReplicate(aePk, aeVersionId, aeDestination)
//  SELECT AgentEntity.agentUid AS aeUid,
//         AgentEntity.agentLct AS aeVersionId,
//         UserSession.usClientNodeId AS aeDestination
//    FROM ChangeLog
//         JOIN AgentEntity
//             ON ChangeLog.chTableId = 68
//                AND ChangeLog.chEntityPk = AgentEntity.agentUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND AgentEntity.agentLct != COALESCE(
//         (SELECT aeVersionId
//            FROM AgentEntityReplicate
//           WHERE aePk = AgentEntity.agentUid
//             AND aeDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(aePk, aeDestination) DO UPDATE
//     SET aePending = true
//  */               
// """)
// @ReplicationRunOnChange([AgentEntity::class])
// @ReplicationCheckPendingNotificationsFor([AgentEntity::class])
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
  primaryKeys = arrayOf("aePk", "aeDestination"),
  indices = arrayOf(Index(value = arrayOf("aePk", "aeDestination", "aeVersionId")),
  Index(value = arrayOf("aeDestination", "aePending")))

)
@Serializable
public class AgentEntityReplicate {
  @ReplicationEntityForeignKey
  public var aePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var aeVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var aeDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var aePending: Boolean = true
}
