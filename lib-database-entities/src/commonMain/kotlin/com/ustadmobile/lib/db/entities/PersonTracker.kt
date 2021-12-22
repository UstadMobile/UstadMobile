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
  primaryKeys = arrayOf("personFk", "personDestination"),
  indices = arrayOf(Index(value = arrayOf("personDestination", "personProcessed", "personFk")))
)
@Serializable
public class PersonTracker {
  @ReplicationEntityForeignKey
  public var personFk: Long = 0

  @ReplicationVersionId
  public var personVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var personDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var personProcessed: Boolean = false
}
