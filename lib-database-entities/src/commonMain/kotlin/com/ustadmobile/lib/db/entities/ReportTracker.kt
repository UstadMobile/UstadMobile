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
  primaryKeys = arrayOf("reportFk", "reportDestination"),
  indices = arrayOf(Index(value = arrayOf("reportDestination", "reportProcessed", "reportFk")))
)
@Serializable
public class ReportTracker {
  @ReplicationEntityForeignKey
  public var reportFk: Long = 0

  @ReplicationVersionId
  public var reportVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var reportDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var reportProcessed: Boolean = false
}
