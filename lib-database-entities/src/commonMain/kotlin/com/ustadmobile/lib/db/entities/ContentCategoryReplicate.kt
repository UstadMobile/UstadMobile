// @Triggers(arrayOf(
//     Trigger(name = "contentcategory_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO ContentCategory(contentCategoryUid, ctnCatContentCategorySchemaUid, name, contentCategoryLocalChangeSeqNum, contentCategoryMasterChangeSeqNum, contentCategoryLastChangedBy, contentCategoryLct) VALUES (NEW.contentCategoryUid, NEW.ctnCatContentCategorySchemaUid, NEW.name, NEW.contentCategoryLocalChangeSeqNum, NEW.contentCategoryMasterChangeSeqNum, NEW.contentCategoryLastChangedBy, NEW.contentCategoryLct) " +
//             "/*psql ON CONFLICT (contentCategoryUid) DO UPDATE SET ctnCatContentCategorySchemaUid = EXCLUDED.ctnCatContentCategorySchemaUid, name = EXCLUDED.name, contentCategoryLocalChangeSeqNum = EXCLUDED.contentCategoryLocalChangeSeqNum, contentCategoryMasterChangeSeqNum = EXCLUDED.contentCategoryMasterChangeSeqNum, contentCategoryLastChangedBy = EXCLUDED.contentCategoryLastChangedBy, contentCategoryLct = EXCLUDED.contentCategoryLct*/"
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
  primaryKeys = arrayOf("ccPk", "ccDestination"),
  indices = arrayOf(Index(value = arrayOf("ccDestination", "ccProcessed", "ccPk")))
)
@Serializable
public class ContentCategoryReplicate {
  @ReplicationEntityForeignKey
  public var ccPk: Long = 0

  @ReplicationVersionId
  public var ccVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ccDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var ccProcessed: Boolean = false
}
