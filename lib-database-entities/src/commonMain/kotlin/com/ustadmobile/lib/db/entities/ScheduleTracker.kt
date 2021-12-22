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
  primaryKeys = arrayOf("scheduleFk", "scheduleDestination"),
  indices = arrayOf(Index(value = arrayOf("scheduleDestination", "scheduleProcessed",
      "scheduleFk")))
)
@Serializable
public class ScheduleTracker {
  @ReplicationEntityForeignKey
  public var scheduleFk: Long = 0

  @ReplicationVersionId
  public var scheduleVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var scheduleDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var scheduleProcessed: Boolean = false
}
