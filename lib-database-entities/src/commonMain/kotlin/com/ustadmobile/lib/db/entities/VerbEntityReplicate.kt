// @Triggers(arrayOf(
//     Trigger(name = "verbentity_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO VerbEntity(verbUid, urlId, verbInActive, verbMasterChangeSeqNum, verbLocalChangeSeqNum, verbLastChangedBy, verbLct) VALUES (NEW.verbUid, NEW.urlId, NEW.verbInActive, NEW.verbMasterChangeSeqNum, NEW.verbLocalChangeSeqNum, NEW.verbLastChangedBy, NEW.verbLct) " +
//             "/*psql ON CONFLICT (verbUid) DO UPDATE SET urlId = EXCLUDED.urlId, verbInActive = EXCLUDED.verbInActive, verbMasterChangeSeqNum = EXCLUDED.verbMasterChangeSeqNum, verbLocalChangeSeqNum = EXCLUDED.verbLocalChangeSeqNum, verbLastChangedBy = EXCLUDED.verbLastChangedBy, verbLct = EXCLUDED.verbLct*/"
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
  primaryKeys = arrayOf("vePk", "veDestination"),
  indices = arrayOf(Index(value = arrayOf("veDestination", "veProcessed", "vePk")))
)
@Serializable
public class VerbEntityReplicate {
  @ReplicationEntityForeignKey
  public var vePk: Long = 0

  @ReplicationVersionId
  public var veVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var veDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var veProcessed: Boolean = false
}
