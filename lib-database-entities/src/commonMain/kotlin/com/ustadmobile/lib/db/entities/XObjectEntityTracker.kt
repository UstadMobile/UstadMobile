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
  primaryKeys = arrayOf("xoeFk", "xoeDestination"),
  indices = arrayOf(Index(value = arrayOf("xoeDestination", "xoeProcessed", "xoeFk")))
)
@Serializable
public class XObjectEntityTracker {
  @ReplicationEntityForeignKey
  public var xoeFk: Long = 0

  @ReplicationVersionId
  public var xoeVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var xoeDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var xoeProcessed: Boolean = false
}
