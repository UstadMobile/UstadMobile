// @Triggers(arrayOf(
//     Trigger(name = "clazzassignmentcontentjoin_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO ClazzAssignmentContentJoin(cacjUid, cacjContentUid, cacjAssignmentUid, cacjActive, cacjMCSN, cacjLCSN, cacjLCB, cacjLct) VALUES (NEW.cacjUid, NEW.cacjContentUid, NEW.cacjAssignmentUid, NEW.cacjActive, NEW.cacjMCSN, NEW.cacjLCSN, NEW.cacjLCB, NEW.cacjLct) " +
//             "/*psql ON CONFLICT (cacjUid) DO UPDATE SET cacjContentUid = EXCLUDED.cacjContentUid, cacjAssignmentUid = EXCLUDED.cacjAssignmentUid, cacjActive = EXCLUDED.cacjActive, cacjMCSN = EXCLUDED.cacjMCSN, cacjLCSN = EXCLUDED.cacjLCSN, cacjLCB = EXCLUDED.cacjLCB, cacjLct = EXCLUDED.cacjLct*/"
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
  primaryKeys = arrayOf("cacjPk", "cacjDestination"),
  indices = arrayOf(Index(value = arrayOf("cacjDestination", "cacjProcessed", "cacjPk")))
)
@Serializable
public class ClazzAssignmentContentJoinReplicate {
  @ReplicationEntityForeignKey
  public var cacjPk: Long = 0

  @ReplicationVersionId
  public var cacjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cacjDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var cacjProcessed: Boolean = false
}
