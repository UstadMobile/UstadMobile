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
  primaryKeys = arrayOf("ppFk", "ppDestination"),
  indices = arrayOf(Index(value = arrayOf("ppDestination", "ppProcessed", "ppFk")))
)
@Serializable
public class PersonPictureTracker {
  @ReplicationEntityForeignKey
  public var ppFk: Long = 0

  @ReplicationVersionId
  public var ppVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ppDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var ppProcessed: Boolean = false
}
