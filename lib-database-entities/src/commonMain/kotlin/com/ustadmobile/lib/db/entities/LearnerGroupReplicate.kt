// @Triggers(arrayOf(
//     Trigger(name = "learnergroup_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO LearnerGroup(learnerGroupUid, learnerGroupName, learnerGroupDescription, learnerGroupActive, learnerGroupMCSN, learnerGroupCSN, learnerGroupLCB, learnerGroupLct) VALUES (NEW.learnerGroupUid, NEW.learnerGroupName, NEW.learnerGroupDescription, NEW.learnerGroupActive, NEW.learnerGroupMCSN, NEW.learnerGroupCSN, NEW.learnerGroupLCB, NEW.learnerGroupLct) " +
//             "/*psql ON CONFLICT (learnerGroupUid) DO UPDATE SET learnerGroupName = EXCLUDED.learnerGroupName, learnerGroupDescription = EXCLUDED.learnerGroupDescription, learnerGroupActive = EXCLUDED.learnerGroupActive, learnerGroupMCSN = EXCLUDED.learnerGroupMCSN, learnerGroupCSN = EXCLUDED.learnerGroupCSN, learnerGroupLCB = EXCLUDED.learnerGroupLCB, learnerGroupLct = EXCLUDED.learnerGroupLct*/"
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
  primaryKeys = arrayOf("lgPk", "lgDestination"),
  indices = arrayOf(Index(value = arrayOf("lgDestination", "lgProcessed", "lgPk")))
)
@Serializable
public class LearnerGroupReplicate {
  @ReplicationEntityForeignKey
  public var lgPk: Long = 0

  @ReplicationVersionId
  public var lgVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var lgDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var lgProcessed: Boolean = false
}
