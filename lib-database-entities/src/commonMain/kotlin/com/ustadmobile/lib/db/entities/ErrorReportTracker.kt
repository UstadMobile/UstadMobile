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
  primaryKeys = arrayOf("erFk", "erDestination"),
  indices = arrayOf(Index(value = arrayOf("erDestination", "erProcessed", "erFk")))
)
@Serializable
public class ErrorReportTracker {
  @ReplicationEntityForeignKey
  public var erFk: Long = 0

  @ReplicationVersionId
  public var erVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var erDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var erProcessed: Boolean = false
}
