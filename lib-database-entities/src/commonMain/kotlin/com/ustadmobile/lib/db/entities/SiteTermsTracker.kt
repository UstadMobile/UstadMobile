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
  primaryKeys = arrayOf("stFk", "stDestination"),
  indices = arrayOf(Index(value = arrayOf("stDestination", "stProcessed", "stFk")))
)
@Serializable
public class SiteTermsTracker {
  @ReplicationEntityForeignKey
  public var stFk: Long = 0

  @ReplicationVersionId
  public var stVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var stDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var stProcessed: Boolean = false
}
