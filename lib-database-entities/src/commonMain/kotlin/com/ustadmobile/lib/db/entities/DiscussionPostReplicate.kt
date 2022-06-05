
package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.`annotation`.ReplicationDestinationNodeId
import com.ustadmobile.door.`annotation`.ReplicationEntityForeignKey
import com.ustadmobile.door.`annotation`.ReplicationPending
import com.ustadmobile.door.`annotation`.ReplicationVersionId
import kotlin.Boolean
import kotlin.Long
import kotlinx.serialization.Serializable

@Entity(
  primaryKeys = arrayOf("discussionPostPk", "discussionPostDestination"),
  indices = arrayOf(Index(value = arrayOf("discussionPostPk", "discussionPostDestination",
    "discussionPostVersionId")),
  Index(value = arrayOf("discussionPostDestination", "discussionPostPending")))

)
@Serializable
public class DiscussionPostReplicate {
  @ReplicationEntityForeignKey
  public var discussionPostPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var discussionPostVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var discussionPostDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var discussionPostPending: Boolean = true
}
