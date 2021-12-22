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
  primaryKeys = arrayOf("glsFk", "glsDestination"),
  indices = arrayOf(Index(value = arrayOf("glsDestination", "glsProcessed", "glsFk")))
)
@Serializable
public class GroupLearningSessionTracker {
  @ReplicationEntityForeignKey
  public var glsFk: Long = 0

  @ReplicationVersionId
  public var glsVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var glsDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var glsProcessed: Boolean = false
}
