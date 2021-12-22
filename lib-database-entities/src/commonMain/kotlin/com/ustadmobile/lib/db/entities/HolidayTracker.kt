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
  primaryKeys = arrayOf("holidayFk", "holidayDestination"),
  indices = arrayOf(Index(value = arrayOf("holidayDestination", "holidayProcessed", "holidayFk")))
)
@Serializable
public class HolidayTracker {
  @ReplicationEntityForeignKey
  public var holidayFk: Long = 0

  @ReplicationVersionId
  public var holidayVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var holidayDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var holidayProcessed: Boolean = false
}
