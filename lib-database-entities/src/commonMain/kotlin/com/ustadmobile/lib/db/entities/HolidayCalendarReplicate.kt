// @Triggers(arrayOf(
//     Trigger(name = "holidaycalendar_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO HolidayCalendar(umCalendarUid, umCalendarName, umCalendarCategory, umCalendarActive, umCalendarMasterChangeSeqNum, umCalendarLocalChangeSeqNum, umCalendarLastChangedBy, umCalendarLct) VALUES (NEW.umCalendarUid, NEW.umCalendarName, NEW.umCalendarCategory, NEW.umCalendarActive, NEW.umCalendarMasterChangeSeqNum, NEW.umCalendarLocalChangeSeqNum, NEW.umCalendarLastChangedBy, NEW.umCalendarLct) " +
//             "/*psql ON CONFLICT (umCalendarUid) DO UPDATE SET umCalendarName = EXCLUDED.umCalendarName, umCalendarCategory = EXCLUDED.umCalendarCategory, umCalendarActive = EXCLUDED.umCalendarActive, umCalendarMasterChangeSeqNum = EXCLUDED.umCalendarMasterChangeSeqNum, umCalendarLocalChangeSeqNum = EXCLUDED.umCalendarLocalChangeSeqNum, umCalendarLastChangedBy = EXCLUDED.umCalendarLastChangedBy, umCalendarLct = EXCLUDED.umCalendarLct*/"
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
  primaryKeys = arrayOf("hcPk", "hcDestination"),
  indices = arrayOf(Index(value = arrayOf("hcDestination", "hcProcessed", "hcPk")))
)
@Serializable
public class HolidayCalendarReplicate {
  @ReplicationEntityForeignKey
  public var hcPk: Long = 0

  @ReplicationVersionId
  public var hcVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var hcDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var hcProcessed: Boolean = false
}
