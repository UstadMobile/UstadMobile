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
  primaryKeys = arrayOf("sceFk", "sceDestination"),
  indices = arrayOf(Index(value = arrayOf("sceDestination", "sceProcessed", "sceFk")))
)
@Serializable
public class StateContentEntityTracker {
  @ReplicationEntityForeignKey
  public var sceFk: Long = 0

  @ReplicationVersionId
  public var sceVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var sceDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var sceProcessed: Boolean = false
}
