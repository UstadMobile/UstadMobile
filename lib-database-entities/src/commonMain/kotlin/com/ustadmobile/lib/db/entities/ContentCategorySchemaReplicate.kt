// @Triggers(arrayOf(
//     Trigger(name = "contentcategoryschema_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO ContentCategorySchema(contentCategorySchemaUid, schemaName, schemaUrl, contentCategorySchemaLocalChangeSeqNum, contentCategorySchemaMasterChangeSeqNum, contentCategorySchemaLastChangedBy, contentCategorySchemaLct) VALUES (NEW.contentCategorySchemaUid, NEW.schemaName, NEW.schemaUrl, NEW.contentCategorySchemaLocalChangeSeqNum, NEW.contentCategorySchemaMasterChangeSeqNum, NEW.contentCategorySchemaLastChangedBy, NEW.contentCategorySchemaLct) " +
//             "/*psql ON CONFLICT (contentCategorySchemaUid) DO UPDATE SET schemaName = EXCLUDED.schemaName, schemaUrl = EXCLUDED.schemaUrl, contentCategorySchemaLocalChangeSeqNum = EXCLUDED.contentCategorySchemaLocalChangeSeqNum, contentCategorySchemaMasterChangeSeqNum = EXCLUDED.contentCategorySchemaMasterChangeSeqNum, contentCategorySchemaLastChangedBy = EXCLUDED.contentCategorySchemaLastChangedBy, contentCategorySchemaLct = EXCLUDED.contentCategorySchemaLct*/"
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
  primaryKeys = arrayOf("ccsPk", "ccsDestination"),
  indices = arrayOf(Index(value = arrayOf("ccsDestination", "ccsProcessed", "ccsPk")))
)
@Serializable
public class ContentCategorySchemaReplicate {
  @ReplicationEntityForeignKey
  public var ccsPk: Long = 0

  @ReplicationVersionId
  public var ccsVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ccsDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var ccsProcessed: Boolean = false
}
