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
  primaryKeys = arrayOf("paFk", "paDestination"),
  indices = arrayOf(Index(value = arrayOf("paDestination", "paProcessed", "paFk")))
)
@Serializable
public class PersonAuth2Tracker {
  @ReplicationEntityForeignKey
  public var paFk: Long = 0

  @ReplicationVersionId
  public var paVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var paDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var paProcessed: Boolean = false
}
