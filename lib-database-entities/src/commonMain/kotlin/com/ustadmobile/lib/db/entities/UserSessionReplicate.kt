// @Triggers(arrayOf(
//     Trigger(name = "usersession_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO UserSession(usUid, usPcsn, usLcsn, usLcb, usLct, usPersonUid, usClientNodeId, usStartTime, usEndTime, usStatus, usReason, usAuth, usSessionType) VALUES (NEW.usUid, NEW.usPcsn, NEW.usLcsn, NEW.usLcb, NEW.usLct, NEW.usPersonUid, NEW.usClientNodeId, NEW.usStartTime, NEW.usEndTime, NEW.usStatus, NEW.usReason, NEW.usAuth, NEW.usSessionType) " +
//             "/*psql ON CONFLICT (usUid) DO UPDATE SET usPcsn = EXCLUDED.usPcsn, usLcsn = EXCLUDED.usLcsn, usLcb = EXCLUDED.usLcb, usLct = EXCLUDED.usLct, usPersonUid = EXCLUDED.usPersonUid, usClientNodeId = EXCLUDED.usClientNodeId, usStartTime = EXCLUDED.usStartTime, usEndTime = EXCLUDED.usEndTime, usStatus = EXCLUDED.usStatus, usReason = EXCLUDED.usReason, usAuth = EXCLUDED.usAuth, usSessionType = EXCLUDED.usSessionType*/"
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
  primaryKeys = arrayOf("usPk", "usDestination"),
  indices = arrayOf(Index(value = arrayOf("usDestination", "usProcessed", "usPk")))
)
@Serializable
public class UserSessionReplicate {
  @ReplicationEntityForeignKey
  public var usPk: Long = 0

  @ReplicationVersionId
  public var usVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var usDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var usProcessed: Boolean = false
}
