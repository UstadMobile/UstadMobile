// @Triggers(arrayOf(
//     Trigger(name = "statecontententity_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO StateContentEntity(stateContentUid, stateContentStateUid, stateContentKey, stateContentValue, isIsactive, stateContentMasterChangeSeqNum, stateContentLocalChangeSeqNum, stateContentLastChangedBy, stateContentLct) VALUES (NEW.stateContentUid, NEW.stateContentStateUid, NEW.stateContentKey, NEW.stateContentValue, NEW.isIsactive, NEW.stateContentMasterChangeSeqNum, NEW.stateContentLocalChangeSeqNum, NEW.stateContentLastChangedBy, NEW.stateContentLct) " +
//             "/*psql ON CONFLICT (stateContentUid) DO UPDATE SET stateContentStateUid = EXCLUDED.stateContentStateUid, stateContentKey = EXCLUDED.stateContentKey, stateContentValue = EXCLUDED.stateContentValue, isIsactive = EXCLUDED.isIsactive, stateContentMasterChangeSeqNum = EXCLUDED.stateContentMasterChangeSeqNum, stateContentLocalChangeSeqNum = EXCLUDED.stateContentLocalChangeSeqNum, stateContentLastChangedBy = EXCLUDED.stateContentLastChangedBy, stateContentLct = EXCLUDED.stateContentLct*/"
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
  primaryKeys = arrayOf("scePk", "sceDestination"),
  indices = arrayOf(Index(value = arrayOf("sceDestination", "sceProcessed", "scePk")))
)
@Serializable
public class StateContentEntityReplicate {
  @ReplicationEntityForeignKey
  public var scePk: Long = 0

  @ReplicationVersionId
  public var sceVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var sceDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var sceProcessed: Boolean = false
}
