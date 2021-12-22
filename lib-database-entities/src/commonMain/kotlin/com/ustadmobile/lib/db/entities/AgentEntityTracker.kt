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
  primaryKeys = arrayOf("aeFk", "aeDestination"),
  indices = arrayOf(Index(value = arrayOf("aeDestination", "aeProcessed", "aeFk")))
)
@Serializable
public class AgentEntityTracker {
  @ReplicationEntityForeignKey
  public var aeFk: Long = 0

  @ReplicationVersionId
  public var aeVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var aeDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var aeProcessed: Boolean = false
}
