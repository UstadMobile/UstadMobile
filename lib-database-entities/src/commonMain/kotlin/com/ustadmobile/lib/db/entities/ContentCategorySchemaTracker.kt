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
  primaryKeys = arrayOf("ccsFk", "ccsDestination"),
  indices = arrayOf(Index(value = arrayOf("ccsDestination", "ccsProcessed", "ccsFk")))
)
@Serializable
public class ContentCategorySchemaTracker {
  @ReplicationEntityForeignKey
  public var ccsFk: Long = 0

  @ReplicationVersionId
  public var ccsVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ccsDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var ccsProcessed: Boolean = false
}
