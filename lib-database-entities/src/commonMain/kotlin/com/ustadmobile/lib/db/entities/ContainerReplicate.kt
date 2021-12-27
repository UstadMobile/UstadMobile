// @Triggers(arrayOf(
//     Trigger(name = "container_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO Container(containerUid, cntLocalCsn, cntMasterCsn, cntLastModBy, cntLct, fileSize, containerContentEntryUid, cntLastModified, mimeType, remarks, mobileOptimized, cntNumEntries) VALUES (NEW.containerUid, NEW.cntLocalCsn, NEW.cntMasterCsn, NEW.cntLastModBy, NEW.cntLct, NEW.fileSize, NEW.containerContentEntryUid, NEW.cntLastModified, NEW.mimeType, NEW.remarks, NEW.mobileOptimized, NEW.cntNumEntries) " +
//             "/*psql ON CONFLICT (containerUid) DO UPDATE SET cntLocalCsn = EXCLUDED.cntLocalCsn, cntMasterCsn = EXCLUDED.cntMasterCsn, cntLastModBy = EXCLUDED.cntLastModBy, cntLct = EXCLUDED.cntLct, fileSize = EXCLUDED.fileSize, containerContentEntryUid = EXCLUDED.containerContentEntryUid, cntLastModified = EXCLUDED.cntLastModified, mimeType = EXCLUDED.mimeType, remarks = EXCLUDED.remarks, mobileOptimized = EXCLUDED.mobileOptimized, cntNumEntries = EXCLUDED.cntNumEntries*/"
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
  primaryKeys = arrayOf("containerPk", "containerDestination"),
  indices = arrayOf(Index(value = arrayOf("containerDestination", "containerProcessed",
      "containerPk")))
)
@Serializable
public class ContainerReplicate {
  @ReplicationEntityForeignKey
  public var containerPk: Long = 0

  @ReplicationVersionId
  public var containerVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var containerDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var containerProcessed: Boolean = false
}
