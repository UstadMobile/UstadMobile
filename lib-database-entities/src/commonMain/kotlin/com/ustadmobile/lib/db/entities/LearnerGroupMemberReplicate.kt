// @Triggers(arrayOf(
//     Trigger(name = "learnergroupmember_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO LearnerGroupMember(learnerGroupMemberUid, learnerGroupMemberPersonUid, learnerGroupMemberLgUid, learnerGroupMemberRole, learnerGroupMemberActive, learnerGroupMemberMCSN, learnerGroupMemberCSN, learnerGroupMemberLCB, learnerGroupMemberLct) VALUES (NEW.learnerGroupMemberUid, NEW.learnerGroupMemberPersonUid, NEW.learnerGroupMemberLgUid, NEW.learnerGroupMemberRole, NEW.learnerGroupMemberActive, NEW.learnerGroupMemberMCSN, NEW.learnerGroupMemberCSN, NEW.learnerGroupMemberLCB, NEW.learnerGroupMemberLct) " +
//             "/*psql ON CONFLICT (learnerGroupMemberUid) DO UPDATE SET learnerGroupMemberPersonUid = EXCLUDED.learnerGroupMemberPersonUid, learnerGroupMemberLgUid = EXCLUDED.learnerGroupMemberLgUid, learnerGroupMemberRole = EXCLUDED.learnerGroupMemberRole, learnerGroupMemberActive = EXCLUDED.learnerGroupMemberActive, learnerGroupMemberMCSN = EXCLUDED.learnerGroupMemberMCSN, learnerGroupMemberCSN = EXCLUDED.learnerGroupMemberCSN, learnerGroupMemberLCB = EXCLUDED.learnerGroupMemberLCB, learnerGroupMemberLct = EXCLUDED.learnerGroupMemberLct*/"
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
  primaryKeys = arrayOf("lgmPk", "lgmDestination"),
  indices = arrayOf(Index(value = arrayOf("lgmDestination", "lgmProcessed", "lgmPk")))
)
@Serializable
public class LearnerGroupMemberReplicate {
  @ReplicationEntityForeignKey
  public var lgmPk: Long = 0

  @ReplicationVersionId
  public var lgmVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var lgmDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var lgmProcessed: Boolean = false
}
