// @Triggers(arrayOf(
//     Trigger(name = "contextxobjectstatementjoin_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO ContextXObjectStatementJoin(contextXObjectStatementJoinUid, contextActivityFlag, contextStatementUid, contextXObjectUid, verbMasterChangeSeqNum, verbLocalChangeSeqNum, verbLastChangedBy, contextXObjectLct) VALUES (NEW.contextXObjectStatementJoinUid, NEW.contextActivityFlag, NEW.contextStatementUid, NEW.contextXObjectUid, NEW.verbMasterChangeSeqNum, NEW.verbLocalChangeSeqNum, NEW.verbLastChangedBy, NEW.contextXObjectLct) " +
//             "/*psql ON CONFLICT (contextXObjectStatementJoinUid) DO UPDATE SET contextActivityFlag = EXCLUDED.contextActivityFlag, contextStatementUid = EXCLUDED.contextStatementUid, contextXObjectUid = EXCLUDED.contextXObjectUid, verbMasterChangeSeqNum = EXCLUDED.verbMasterChangeSeqNum, verbLocalChangeSeqNum = EXCLUDED.verbLocalChangeSeqNum, verbLastChangedBy = EXCLUDED.verbLastChangedBy, contextXObjectLct = EXCLUDED.contextXObjectLct*/"
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
  primaryKeys = arrayOf("cxosjPk", "cxosjDestination"),
  indices = arrayOf(Index(value = arrayOf("cxosjDestination", "cxosjProcessed", "cxosjPk")))
)
@Serializable
public class ContextXObjectStatementJoinReplicate {
  @ReplicationEntityForeignKey
  public var cxosjPk: Long = 0

  @ReplicationVersionId
  public var cxosjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cxosjDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var cxosjProcessed: Boolean = false
}
