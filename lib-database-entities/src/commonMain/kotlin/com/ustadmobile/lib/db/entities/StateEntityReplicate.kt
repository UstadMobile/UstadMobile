// @Triggers(arrayOf(
//     Trigger(name = "stateentity_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO StateEntity(stateUid, stateId, agentUid, activityId, registration, isIsactive, timestamp, stateMasterChangeSeqNum, stateLocalChangeSeqNum, stateLastChangedBy, stateLct) VALUES (NEW.stateUid, NEW.stateId, NEW.agentUid, NEW.activityId, NEW.registration, NEW.isIsactive, NEW.timestamp, NEW.stateMasterChangeSeqNum, NEW.stateLocalChangeSeqNum, NEW.stateLastChangedBy, NEW.stateLct) " +
//             "/*psql ON CONFLICT (stateUid) DO UPDATE SET stateId = EXCLUDED.stateId, agentUid = EXCLUDED.agentUid, activityId = EXCLUDED.activityId, registration = EXCLUDED.registration, isIsactive = EXCLUDED.isIsactive, timestamp = EXCLUDED.timestamp, stateMasterChangeSeqNum = EXCLUDED.stateMasterChangeSeqNum, stateLocalChangeSeqNum = EXCLUDED.stateLocalChangeSeqNum, stateLastChangedBy = EXCLUDED.stateLastChangedBy, stateLct = EXCLUDED.stateLct*/"
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
  primaryKeys = arrayOf("sePk", "seDestination"),
  indices = arrayOf(Index(value = arrayOf("seDestination", "seProcessed", "sePk")))
)
@Serializable
public class StateEntityReplicate {
  @ReplicationEntityForeignKey
  public var sePk: Long = 0

  @ReplicationVersionId
  public var seVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var seDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var seProcessed: Boolean = false
}
