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
  primaryKeys = arrayOf("sgFk", "sgDestination"),
  indices = arrayOf(Index(value = arrayOf("sgDestination", "sgProcessed", "sgFk")))
)
@Serializable
public class ScopedGrantTracker {
  @ReplicationEntityForeignKey
  public var sgFk: Long = 0

  @ReplicationVersionId
  public var sgVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var sgDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var sgProcessed: Boolean = false
}
