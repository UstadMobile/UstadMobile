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
  primaryKeys = arrayOf("lvFk", "lvDestination"),
  indices = arrayOf(Index(value = arrayOf("lvDestination", "lvProcessed", "lvFk")))
)
@Serializable
public class LanguageVariantTracker {
  @ReplicationEntityForeignKey
  public var lvFk: Long = 0

  @ReplicationVersionId
  public var lvVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var lvDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var lvProcessed: Boolean = false
}
