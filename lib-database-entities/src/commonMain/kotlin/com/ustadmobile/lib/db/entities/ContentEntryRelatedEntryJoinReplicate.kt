// @Triggers(arrayOf(
//     Trigger(name = "contententryrelatedentryjoin_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO ContentEntryRelatedEntryJoin(cerejUid, cerejContentEntryUid, cerejRelatedEntryUid, cerejLastChangedBy, relType, comment, cerejRelLanguageUid, cerejLocalChangeSeqNum, cerejMasterChangeSeqNum, cerejLct) VALUES (NEW.cerejUid, NEW.cerejContentEntryUid, NEW.cerejRelatedEntryUid, NEW.cerejLastChangedBy, NEW.relType, NEW.comment, NEW.cerejRelLanguageUid, NEW.cerejLocalChangeSeqNum, NEW.cerejMasterChangeSeqNum, NEW.cerejLct) " +
//             "/*psql ON CONFLICT (cerejUid) DO UPDATE SET cerejContentEntryUid = EXCLUDED.cerejContentEntryUid, cerejRelatedEntryUid = EXCLUDED.cerejRelatedEntryUid, cerejLastChangedBy = EXCLUDED.cerejLastChangedBy, relType = EXCLUDED.relType, comment = EXCLUDED.comment, cerejRelLanguageUid = EXCLUDED.cerejRelLanguageUid, cerejLocalChangeSeqNum = EXCLUDED.cerejLocalChangeSeqNum, cerejMasterChangeSeqNum = EXCLUDED.cerejMasterChangeSeqNum, cerejLct = EXCLUDED.cerejLct*/"
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
  primaryKeys = arrayOf("cerejPk", "cerejDestination"),
  indices = arrayOf(Index(value = arrayOf("cerejDestination", "cerejProcessed", "cerejPk")))
)
@Serializable
public class ContentEntryRelatedEntryJoinReplicate {
  @ReplicationEntityForeignKey
  public var cerejPk: Long = 0

  @ReplicationVersionId
  public var cerejVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cerejDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var cerejProcessed: Boolean = false
}
