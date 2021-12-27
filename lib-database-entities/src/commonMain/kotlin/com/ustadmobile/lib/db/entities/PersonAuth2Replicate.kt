// @Triggers(arrayOf(
//     Trigger(name = "personauth2_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO PersonAuth2(pauthUid, pauthMechanism, pauthAuth, pauthLcsn, pauthPcsn, pauthLcb, pauthLct) VALUES (NEW.pauthUid, NEW.pauthMechanism, NEW.pauthAuth, NEW.pauthLcsn, NEW.pauthPcsn, NEW.pauthLcb, NEW.pauthLct) " +
//             "/*psql ON CONFLICT (pauthUid) DO UPDATE SET pauthMechanism = EXCLUDED.pauthMechanism, pauthAuth = EXCLUDED.pauthAuth, pauthLcsn = EXCLUDED.pauthLcsn, pauthPcsn = EXCLUDED.pauthPcsn, pauthLcb = EXCLUDED.pauthLcb, pauthLct = EXCLUDED.pauthLct*/"
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
  primaryKeys = arrayOf("paPk", "paDestination"),
  indices = arrayOf(Index(value = arrayOf("paDestination", "paProcessed", "paPk")))
)
@Serializable
public class PersonAuth2Replicate {
  @ReplicationEntityForeignKey
  public var paPk: Long = 0

  @ReplicationVersionId
  public var paVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var paDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var paProcessed: Boolean = false
}
