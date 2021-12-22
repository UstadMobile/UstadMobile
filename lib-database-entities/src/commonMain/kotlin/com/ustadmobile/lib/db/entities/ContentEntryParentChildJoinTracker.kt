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
  primaryKeys = arrayOf("cepcjFk", "cepcjDestination"),
  indices = arrayOf(Index(value = arrayOf("cepcjDestination", "cepcjProcessed", "cepcjFk")))
)
@Serializable
public class ContentEntryParentChildJoinTracker {
  @ReplicationEntityForeignKey
  public var cepcjFk: Long = 0

  @ReplicationVersionId
  public var cepcjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cepcjDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var cepcjProcessed: Boolean = false
}
