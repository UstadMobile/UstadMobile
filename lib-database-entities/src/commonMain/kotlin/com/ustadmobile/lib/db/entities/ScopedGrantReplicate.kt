// @Triggers(arrayOf(
//     Trigger(name = "scopedgrant_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO ScopedGrant(sgUid, sgPcsn, sgLcsn, sgLcb, sgLct, sgTableId, sgEntityUid, sgPermissions, sgGroupUid, sgIndex, sgFlags) VALUES (NEW.sgUid, NEW.sgPcsn, NEW.sgLcsn, NEW.sgLcb, NEW.sgLct, NEW.sgTableId, NEW.sgEntityUid, NEW.sgPermissions, NEW.sgGroupUid, NEW.sgIndex, NEW.sgFlags) " +
//             "/*psql ON CONFLICT (sgUid) DO UPDATE SET sgPcsn = EXCLUDED.sgPcsn, sgLcsn = EXCLUDED.sgLcsn, sgLcb = EXCLUDED.sgLcb, sgLct = EXCLUDED.sgLct, sgTableId = EXCLUDED.sgTableId, sgEntityUid = EXCLUDED.sgEntityUid, sgPermissions = EXCLUDED.sgPermissions, sgGroupUid = EXCLUDED.sgGroupUid, sgIndex = EXCLUDED.sgIndex, sgFlags = EXCLUDED.sgFlags*/"
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
  primaryKeys = arrayOf("sgPk", "sgDestination"),
  indices = arrayOf(Index(value = arrayOf("sgDestination", "sgProcessed", "sgPk")))
)
@Serializable
public class ScopedGrantReplicate {
  @ReplicationEntityForeignKey
  public var sgPk: Long = 0

  @ReplicationVersionId
  public var sgVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var sgDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var sgProcessed: Boolean = false
}
