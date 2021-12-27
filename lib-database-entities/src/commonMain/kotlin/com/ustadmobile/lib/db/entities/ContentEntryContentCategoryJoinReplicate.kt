// @Triggers(arrayOf(
//     Trigger(name = "contententrycontentcategoryjoin_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO ContentEntryContentCategoryJoin(ceccjUid, ceccjContentEntryUid, ceccjContentCategoryUid, ceccjLocalChangeSeqNum, ceccjMasterChangeSeqNum, ceccjLastChangedBy, ceccjLct) VALUES (NEW.ceccjUid, NEW.ceccjContentEntryUid, NEW.ceccjContentCategoryUid, NEW.ceccjLocalChangeSeqNum, NEW.ceccjMasterChangeSeqNum, NEW.ceccjLastChangedBy, NEW.ceccjLct) " +
//             "/*psql ON CONFLICT (ceccjUid) DO UPDATE SET ceccjContentEntryUid = EXCLUDED.ceccjContentEntryUid, ceccjContentCategoryUid = EXCLUDED.ceccjContentCategoryUid, ceccjLocalChangeSeqNum = EXCLUDED.ceccjLocalChangeSeqNum, ceccjMasterChangeSeqNum = EXCLUDED.ceccjMasterChangeSeqNum, ceccjLastChangedBy = EXCLUDED.ceccjLastChangedBy, ceccjLct = EXCLUDED.ceccjLct*/"
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
  primaryKeys = arrayOf("ceccjPk", "ceccjDestination"),
  indices = arrayOf(Index(value = arrayOf("ceccjDestination", "ceccjProcessed", "ceccjPk")))
)
@Serializable
public class ContentEntryContentCategoryJoinReplicate {
  @ReplicationEntityForeignKey
  public var ceccjPk: Long = 0

  @ReplicationVersionId
  public var ceccjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ceccjDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var ceccjProcessed: Boolean = false
}
