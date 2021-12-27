// @Triggers(arrayOf(
//     Trigger(name = "contententryparentchildjoin_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO ContentEntryParentChildJoin(cepcjParentContentEntryUid, cepcjChildContentEntryUid, childIndex, cepcjUid, cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum, cepcjLastChangedBy, cepcjLct) VALUES (NEW.cepcjParentContentEntryUid, NEW.cepcjChildContentEntryUid, NEW.childIndex, NEW.cepcjUid, NEW.cepcjLocalChangeSeqNum, NEW.cepcjMasterChangeSeqNum, NEW.cepcjLastChangedBy, NEW.cepcjLct) " +
//             "/*psql ON CONFLICT (cepcjUid) DO UPDATE SET cepcjParentContentEntryUid = EXCLUDED.cepcjParentContentEntryUid, cepcjChildContentEntryUid = EXCLUDED.cepcjChildContentEntryUid, childIndex = EXCLUDED.childIndex, cepcjLocalChangeSeqNum = EXCLUDED.cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum = EXCLUDED.cepcjMasterChangeSeqNum, cepcjLastChangedBy = EXCLUDED.cepcjLastChangedBy, cepcjLct = EXCLUDED.cepcjLct*/"
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
  primaryKeys = arrayOf("cepcjPk", "cepcjDestination"),
  indices = arrayOf(Index(value = arrayOf("cepcjDestination", "cepcjProcessed", "cepcjPk")))
)
@Serializable
public class ContentEntryParentChildJoinReplicate {
  @ReplicationEntityForeignKey
  public var cepcjPk: Long = 0

  @ReplicationVersionId
  public var cepcjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cepcjDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var cepcjProcessed: Boolean = false
}
