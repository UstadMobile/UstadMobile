// @Triggers(arrayOf(
//     Trigger(name = "leavingreason_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO LeavingReason(leavingReasonUid, leavingReasonTitle, leavingReasonMCSN, leavingReasonCSN, leavingReasonLCB, leavingReasonLct) VALUES (NEW.leavingReasonUid, NEW.leavingReasonTitle, NEW.leavingReasonMCSN, NEW.leavingReasonCSN, NEW.leavingReasonLCB, NEW.leavingReasonLct) " +
//             "/*psql ON CONFLICT (leavingReasonUid) DO UPDATE SET leavingReasonTitle = EXCLUDED.leavingReasonTitle, leavingReasonMCSN = EXCLUDED.leavingReasonMCSN, leavingReasonCSN = EXCLUDED.leavingReasonCSN, leavingReasonLCB = EXCLUDED.leavingReasonLCB, leavingReasonLct = EXCLUDED.leavingReasonLct*/"
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
  primaryKeys = arrayOf("lrPk", "lrDestination"),
  indices = arrayOf(Index(value = arrayOf("lrDestination", "lrProcessed", "lrPk")))
)
@Serializable
public class LeavingReasonReplicate {
  @ReplicationEntityForeignKey
  public var lrPk: Long = 0

  @ReplicationVersionId
  public var lrVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var lrDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var lrProcessed: Boolean = false
}
