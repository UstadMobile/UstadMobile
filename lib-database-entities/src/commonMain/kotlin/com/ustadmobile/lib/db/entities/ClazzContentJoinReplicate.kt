// @Triggers(arrayOf(
//     Trigger(name = "clazzcontentjoin_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO ClazzContentJoin(ccjUid, ccjContentEntryUid, ccjClazzUid, ccjActive, ccjLocalChangeSeqNum, ccjMasterChangeSeqNum, ccjLastChangedBy, ccjLct) VALUES (NEW.ccjUid, NEW.ccjContentEntryUid, NEW.ccjClazzUid, NEW.ccjActive, NEW.ccjLocalChangeSeqNum, NEW.ccjMasterChangeSeqNum, NEW.ccjLastChangedBy, NEW.ccjLct) " +
//             "/*psql ON CONFLICT (ccjUid) DO UPDATE SET ccjContentEntryUid = EXCLUDED.ccjContentEntryUid, ccjClazzUid = EXCLUDED.ccjClazzUid, ccjActive = EXCLUDED.ccjActive, ccjLocalChangeSeqNum = EXCLUDED.ccjLocalChangeSeqNum, ccjMasterChangeSeqNum = EXCLUDED.ccjMasterChangeSeqNum, ccjLastChangedBy = EXCLUDED.ccjLastChangedBy, ccjLct = EXCLUDED.ccjLct*/"
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
  primaryKeys = arrayOf("ccjPk", "ccjDestination"),
  indices = arrayOf(Index(value = arrayOf("ccjDestination", "ccjProcessed", "ccjPk")))
)
@Serializable
public class ClazzContentJoinReplicate {
  @ReplicationEntityForeignKey
  public var ccjPk: Long = 0

  @ReplicationVersionId
  public var ccjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ccjDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var ccjProcessed: Boolean = false
}
