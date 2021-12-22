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
  primaryKeys = arrayOf("clazzFk", "clazzDestination"),
  indices = arrayOf(Index(value = arrayOf("clazzDestination", "clazzProcessed", "clazzFk")))
)
@Serializable
public class ClazzTracker {
  @ReplicationEntityForeignKey
  public var clazzFk: Long = 0

  @ReplicationVersionId
  public var clazzVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var clazzDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var clazzProcessed: Boolean = false
}
