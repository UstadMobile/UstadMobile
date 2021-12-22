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
  primaryKeys = arrayOf("pgFk", "pgDestination"),
  indices = arrayOf(Index(value = arrayOf("pgDestination", "pgProcessed", "pgFk")))
)
@Serializable
public class PersonGroupTracker {
  @ReplicationEntityForeignKey
  public var pgFk: Long = 0

  @ReplicationVersionId
  public var pgVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var pgDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var pgProcessed: Boolean = false
}
