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
  primaryKeys = arrayOf("clFk", "clDestination"),
  indices = arrayOf(Index(value = arrayOf("clDestination", "clProcessed", "clFk")))
)
@Serializable
public class ClazzLogTracker {
  @ReplicationEntityForeignKey
  public var clFk: Long = 0

  @ReplicationVersionId
  public var clVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var clDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var clProcessed: Boolean = false
}
