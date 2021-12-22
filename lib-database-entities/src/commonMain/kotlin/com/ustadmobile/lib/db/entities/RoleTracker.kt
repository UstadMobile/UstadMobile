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
  primaryKeys = arrayOf("roleFk", "roleDestination"),
  indices = arrayOf(Index(value = arrayOf("roleDestination", "roleProcessed", "roleFk")))
)
@Serializable
public class RoleTracker {
  @ReplicationEntityForeignKey
  public var roleFk: Long = 0

  @ReplicationVersionId
  public var roleVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var roleDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var roleProcessed: Boolean = false
}
