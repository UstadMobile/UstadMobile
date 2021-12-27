// @Triggers(arrayOf(
//     Trigger(name = "persongroup_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO PersonGroup(groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupLct, groupName, groupActive, personGroupFlag) VALUES (NEW.groupUid, NEW.groupMasterCsn, NEW.groupLocalCsn, NEW.groupLastChangedBy, NEW.groupLct, NEW.groupName, NEW.groupActive, NEW.personGroupFlag) " +
//             "/*psql ON CONFLICT (groupUid) DO UPDATE SET groupMasterCsn = EXCLUDED.groupMasterCsn, groupLocalCsn = EXCLUDED.groupLocalCsn, groupLastChangedBy = EXCLUDED.groupLastChangedBy, groupLct = EXCLUDED.groupLct, groupName = EXCLUDED.groupName, groupActive = EXCLUDED.groupActive, personGroupFlag = EXCLUDED.personGroupFlag*/"
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
  primaryKeys = arrayOf("pgPk", "pgDestination"),
  indices = arrayOf(Index(value = arrayOf("pgDestination", "pgProcessed", "pgPk")))
)
@Serializable
public class PersonGroupReplicate {
  @ReplicationEntityForeignKey
  public var pgPk: Long = 0

  @ReplicationVersionId
  public var pgVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var pgDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var pgProcessed: Boolean = false
}
