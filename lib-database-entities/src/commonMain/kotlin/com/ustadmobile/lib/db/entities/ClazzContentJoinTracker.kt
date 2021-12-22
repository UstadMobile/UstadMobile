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
  primaryKeys = arrayOf("ccjFk", "ccjDestination"),
  indices = arrayOf(Index(value = arrayOf("ccjDestination", "ccjProcessed", "ccjFk")))
)
@Serializable
public class ClazzContentJoinTracker {
  @ReplicationEntityForeignKey
  public var ccjFk: Long = 0

  @ReplicationVersionId
  public var ccjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ccjDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var ccjProcessed: Boolean = false
}
