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
  primaryKeys = arrayOf("xlmeFk", "xlmeDestination"),
  indices = arrayOf(Index(value = arrayOf("xlmeDestination", "xlmeProcessed", "xlmeFk")))
)
@Serializable
public class XLangMapEntryTracker {
  @ReplicationEntityForeignKey
  public var xlmeFk: Long = 0

  @ReplicationVersionId
  public var xlmeVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var xlmeDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var xlmeProcessed: Boolean = false
}
