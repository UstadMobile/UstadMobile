// @Triggers(arrayOf(
//     Trigger(name = "agententity_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO AgentEntity(agentUid, agentMbox, agentMbox_sha1sum, agentOpenid, agentAccountName, agentHomePage, agentPersonUid, statementMasterChangeSeqNum, statementLocalChangeSeqNum, statementLastChangedBy, agentLct) VALUES (NEW.agentUid, NEW.agentMbox, NEW.agentMbox_sha1sum, NEW.agentOpenid, NEW.agentAccountName, NEW.agentHomePage, NEW.agentPersonUid, NEW.statementMasterChangeSeqNum, NEW.statementLocalChangeSeqNum, NEW.statementLastChangedBy, NEW.agentLct) " +
//             "/*psql ON CONFLICT (agentUid) DO UPDATE SET agentMbox = EXCLUDED.agentMbox, agentMbox_sha1sum = EXCLUDED.agentMbox_sha1sum, agentOpenid = EXCLUDED.agentOpenid, agentAccountName = EXCLUDED.agentAccountName, agentHomePage = EXCLUDED.agentHomePage, agentPersonUid = EXCLUDED.agentPersonUid, statementMasterChangeSeqNum = EXCLUDED.statementMasterChangeSeqNum, statementLocalChangeSeqNum = EXCLUDED.statementLocalChangeSeqNum, statementLastChangedBy = EXCLUDED.statementLastChangedBy, agentLct = EXCLUDED.agentLct*/"
//             ])
//     )
// )            
package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.`annotation`.ReplicationDestinationNodeId
import com.ustadmobile.door.`annotation`.ReplicationEntityForeignKey
import com.ustadmobile.door.`annotation`.ReplicationTrackerProcessed
import com.ustadmobile.door.`annotation`.ReplicationVersionId
import kotlin.Boolean
import kotlin.Long
import kotlinx.serialization.Serializable

@Entity(
  primaryKeys = arrayOf("aePk", "aeDestination"),
  indices = arrayOf(Index(value = arrayOf("aeDestination", "aeProcessed", "aePk")))
)
@Serializable
public class AgentEntityReplicate {
  @ReplicationEntityForeignKey
  public var aePk: Long = 0

  @ReplicationVersionId
  public var aeVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var aeDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var aeProcessed: Boolean = false
}
