// @Triggers(arrayOf(
//     Trigger(name = "persongroupmember_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO PersonGroupMember(groupMemberUid, groupMemberActive, groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy, groupMemberLct) VALUES (NEW.groupMemberUid, NEW.groupMemberActive, NEW.groupMemberPersonUid, NEW.groupMemberGroupUid, NEW.groupMemberMasterCsn, NEW.groupMemberLocalCsn, NEW.groupMemberLastChangedBy, NEW.groupMemberLct) " +
//             "/*psql ON CONFLICT (groupMemberUid) DO UPDATE SET groupMemberActive = EXCLUDED.groupMemberActive, groupMemberPersonUid = EXCLUDED.groupMemberPersonUid, groupMemberGroupUid = EXCLUDED.groupMemberGroupUid, groupMemberMasterCsn = EXCLUDED.groupMemberMasterCsn, groupMemberLocalCsn = EXCLUDED.groupMemberLocalCsn, groupMemberLastChangedBy = EXCLUDED.groupMemberLastChangedBy, groupMemberLct = EXCLUDED.groupMemberLct*/"
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
  primaryKeys = arrayOf("pgmPk", "pgmDestination"),
  indices = arrayOf(Index(value = arrayOf("pgmDestination", "pgmProcessed", "pgmPk")))
)
@Serializable
public class PersonGroupMemberReplicate {
  @ReplicationEntityForeignKey
  public var pgmPk: Long = 0

  @ReplicationVersionId
  public var pgmVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var pgmDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var pgmProcessed: Boolean = false
}
