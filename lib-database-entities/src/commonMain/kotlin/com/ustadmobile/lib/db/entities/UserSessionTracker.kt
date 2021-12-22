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
  primaryKeys = arrayOf("usFk", "usDestination"),
  indices = arrayOf(Index(value = arrayOf("usDestination", "usProcessed", "usFk")))
)
@Serializable
public class UserSessionTracker {
  @ReplicationEntityForeignKey
  public var usFk: Long = 0

  @ReplicationVersionId
  public var usVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var usDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var usProcessed: Boolean = false
}
