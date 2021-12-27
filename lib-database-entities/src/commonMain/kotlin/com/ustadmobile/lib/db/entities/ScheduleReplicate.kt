// @Triggers(arrayOf(
//     Trigger(name = "schedule_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO Schedule(scheduleUid, sceduleStartTime, scheduleEndTime, scheduleDay, scheduleMonth, scheduleFrequency, umCalendarUid, scheduleClazzUid, scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum, scheduleLastChangedBy, scheduleLastChangedTime, scheduleActive) VALUES (NEW.scheduleUid, NEW.sceduleStartTime, NEW.scheduleEndTime, NEW.scheduleDay, NEW.scheduleMonth, NEW.scheduleFrequency, NEW.umCalendarUid, NEW.scheduleClazzUid, NEW.scheduleMasterChangeSeqNum, NEW.scheduleLocalChangeSeqNum, NEW.scheduleLastChangedBy, NEW.scheduleLastChangedTime, NEW.scheduleActive) " +
//             "/*psql ON CONFLICT (scheduleUid) DO UPDATE SET sceduleStartTime = EXCLUDED.sceduleStartTime, scheduleEndTime = EXCLUDED.scheduleEndTime, scheduleDay = EXCLUDED.scheduleDay, scheduleMonth = EXCLUDED.scheduleMonth, scheduleFrequency = EXCLUDED.scheduleFrequency, umCalendarUid = EXCLUDED.umCalendarUid, scheduleClazzUid = EXCLUDED.scheduleClazzUid, scheduleMasterChangeSeqNum = EXCLUDED.scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum = EXCLUDED.scheduleLocalChangeSeqNum, scheduleLastChangedBy = EXCLUDED.scheduleLastChangedBy, scheduleLastChangedTime = EXCLUDED.scheduleLastChangedTime, scheduleActive = EXCLUDED.scheduleActive*/"
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
  primaryKeys = arrayOf("schedulePk", "scheduleDestination"),
  indices = arrayOf(Index(value = arrayOf("scheduleDestination", "scheduleProcessed",
      "schedulePk")))
)
@Serializable
public class ScheduleReplicate {
  @ReplicationEntityForeignKey
  public var schedulePk: Long = 0

  @ReplicationVersionId
  public var scheduleVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var scheduleDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var scheduleProcessed: Boolean = false
}
