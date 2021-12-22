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
  primaryKeys = arrayOf("caFk", "caDestination"),
  indices = arrayOf(Index(value = arrayOf("caDestination", "caProcessed", "caFk")))
)
@Serializable
public class ClazzAssignmentTracker {
  @ReplicationEntityForeignKey
  public var caFk: Long = 0

  @ReplicationVersionId
  public var caVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var caDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var caProcessed: Boolean = false
}
