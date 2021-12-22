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
  primaryKeys = arrayOf("commentsFk", "commentsDestination"),
  indices = arrayOf(Index(value = arrayOf("commentsDestination", "commentsProcessed",
      "commentsFk")))
)
@Serializable
public class CommentsTracker {
  @ReplicationEntityForeignKey
  public var commentsFk: Long = 0

  @ReplicationVersionId
  public var commentsVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var commentsDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var commentsProcessed: Boolean = false
}
