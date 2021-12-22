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
  primaryKeys = arrayOf("lgmFk", "lgmDestination"),
  indices = arrayOf(Index(value = arrayOf("lgmDestination", "lgmProcessed", "lgmFk")))
)
@Serializable
public class LearnerGroupMemberTracker {
  @ReplicationEntityForeignKey
  public var lgmFk: Long = 0

  @ReplicationVersionId
  public var lgmVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var lgmDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var lgmProcessed: Boolean = false
}
