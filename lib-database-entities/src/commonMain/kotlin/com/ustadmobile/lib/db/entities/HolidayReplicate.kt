// @Triggers(arrayOf(
//     Trigger(name = "holiday_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO Holiday(holUid, holMasterCsn, holLocalCsn, holLastModBy, holLct, holActive, holHolidayCalendarUid, holStartTime, holEndTime, holName) VALUES (NEW.holUid, NEW.holMasterCsn, NEW.holLocalCsn, NEW.holLastModBy, NEW.holLct, NEW.holActive, NEW.holHolidayCalendarUid, NEW.holStartTime, NEW.holEndTime, NEW.holName) " +
//             "/*psql ON CONFLICT (holUid) DO UPDATE SET holMasterCsn = EXCLUDED.holMasterCsn, holLocalCsn = EXCLUDED.holLocalCsn, holLastModBy = EXCLUDED.holLastModBy, holLct = EXCLUDED.holLct, holActive = EXCLUDED.holActive, holHolidayCalendarUid = EXCLUDED.holHolidayCalendarUid, holStartTime = EXCLUDED.holStartTime, holEndTime = EXCLUDED.holEndTime, holName = EXCLUDED.holName*/"
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
  primaryKeys = arrayOf("holidayPk", "holidayDestination"),
  indices = arrayOf(Index(value = arrayOf("holidayDestination", "holidayProcessed", "holidayPk")))
)
@Serializable
public class HolidayReplicate {
  @ReplicationEntityForeignKey
  public var holidayPk: Long = 0

  @ReplicationVersionId
  public var holidayVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var holidayDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var holidayProcessed: Boolean = false
}
