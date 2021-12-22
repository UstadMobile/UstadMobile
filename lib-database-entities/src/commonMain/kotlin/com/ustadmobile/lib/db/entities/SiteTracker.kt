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
  primaryKeys = arrayOf("siteFk", "siteDestination"),
  indices = arrayOf(Index(value = arrayOf("siteDestination", "siteProcessed", "siteFk")))
)
@Serializable
public class SiteTracker {
  @ReplicationEntityForeignKey
  public var siteFk: Long = 0

  @ReplicationVersionId
  public var siteVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var siteDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var siteProcessed: Boolean = false
}
