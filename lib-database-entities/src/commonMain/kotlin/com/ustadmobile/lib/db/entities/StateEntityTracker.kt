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
  primaryKeys = arrayOf("seFk", "seDestination"),
  indices = arrayOf(Index(value = arrayOf("seDestination", "seProcessed", "seFk")))
)
@Serializable
public class StateEntityTracker {
  @ReplicationEntityForeignKey
  public var seFk: Long = 0

  @ReplicationVersionId
  public var seVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var seDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var seProcessed: Boolean = false
}
