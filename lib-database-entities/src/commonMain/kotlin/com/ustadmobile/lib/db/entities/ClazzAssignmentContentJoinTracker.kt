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
  primaryKeys = arrayOf("cacjFk", "cacjDestination"),
  indices = arrayOf(Index(value = arrayOf("cacjDestination", "cacjProcessed", "cacjFk")))
)
@Serializable
public class ClazzAssignmentContentJoinTracker {
  @ReplicationEntityForeignKey
  public var cacjFk: Long = 0

  @ReplicationVersionId
  public var cacjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cacjDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var cacjProcessed: Boolean = false
}
