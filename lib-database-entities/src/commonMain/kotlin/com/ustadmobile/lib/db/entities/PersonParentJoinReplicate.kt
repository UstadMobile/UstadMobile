// @Triggers(arrayOf(
//     Trigger(name = "personparentjoin_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO PersonParentJoin(ppjUid, ppjPcsn, ppjLcsn, ppjLcb, ppjLct, ppjParentPersonUid, ppjMinorPersonUid, ppjRelationship, ppjEmail, ppjPhone, ppjInactive, ppjStatus, ppjApprovalTiemstamp, ppjApprovalIpAddr) VALUES (NEW.ppjUid, NEW.ppjPcsn, NEW.ppjLcsn, NEW.ppjLcb, NEW.ppjLct, NEW.ppjParentPersonUid, NEW.ppjMinorPersonUid, NEW.ppjRelationship, NEW.ppjEmail, NEW.ppjPhone, NEW.ppjInactive, NEW.ppjStatus, NEW.ppjApprovalTiemstamp, NEW.ppjApprovalIpAddr) " +
//             "/*psql ON CONFLICT (ppjUid) DO UPDATE SET ppjPcsn = EXCLUDED.ppjPcsn, ppjLcsn = EXCLUDED.ppjLcsn, ppjLcb = EXCLUDED.ppjLcb, ppjLct = EXCLUDED.ppjLct, ppjParentPersonUid = EXCLUDED.ppjParentPersonUid, ppjMinorPersonUid = EXCLUDED.ppjMinorPersonUid, ppjRelationship = EXCLUDED.ppjRelationship, ppjEmail = EXCLUDED.ppjEmail, ppjPhone = EXCLUDED.ppjPhone, ppjInactive = EXCLUDED.ppjInactive, ppjStatus = EXCLUDED.ppjStatus, ppjApprovalTiemstamp = EXCLUDED.ppjApprovalTiemstamp, ppjApprovalIpAddr = EXCLUDED.ppjApprovalIpAddr*/"
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
  primaryKeys = arrayOf("ppjPk", "ppjDestination"),
  indices = arrayOf(Index(value = arrayOf("ppjDestination", "ppjProcessed", "ppjPk")))
)
@Serializable
public class PersonParentJoinReplicate {
  @ReplicationEntityForeignKey
  public var ppjPk: Long = 0

  @ReplicationVersionId
  public var ppjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ppjDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var ppjProcessed: Boolean = false
}
