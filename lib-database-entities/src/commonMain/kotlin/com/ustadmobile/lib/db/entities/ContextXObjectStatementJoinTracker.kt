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
  primaryKeys = arrayOf("cxosjFk", "cxosjDestination"),
  indices = arrayOf(Index(value = arrayOf("cxosjDestination", "cxosjProcessed", "cxosjFk")))
)
@Serializable
public class ContextXObjectStatementJoinTracker {
  @ReplicationEntityForeignKey
  public var cxosjFk: Long = 0

  @ReplicationVersionId
  public var cxosjVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cxosjDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var cxosjProcessed: Boolean = false
}
