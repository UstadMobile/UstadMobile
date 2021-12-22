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
  primaryKeys = arrayOf("smFk", "smDestination"),
  indices = arrayOf(Index(value = arrayOf("smDestination", "smProcessed", "smFk")))
)
@Serializable
public class SchoolMemberTracker {
  @ReplicationEntityForeignKey
  public var smFk: Long = 0

  @ReplicationVersionId
  public var smVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var smDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var smProcessed: Boolean = false
}
