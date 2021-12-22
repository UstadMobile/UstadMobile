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
  primaryKeys = arrayOf("cerejFk", "cerejDestination"),
  indices = arrayOf(Index(value = arrayOf("cerejDestination", "cerejProcessed", "cerejFk")))
)
@Serializable
public class ContentEntryRelatedEntryJoinTracker {
  @ReplicationEntityForeignKey
  public var cerejFk: Long = 0

  @ReplicationVersionId
  public var cerejVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cerejDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var cerejProcessed: Boolean = false
}
