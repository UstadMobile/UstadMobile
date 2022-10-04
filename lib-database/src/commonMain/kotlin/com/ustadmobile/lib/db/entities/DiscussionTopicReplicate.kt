
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
  primaryKeys = arrayOf("discussionTopicPk", "discussionTopicDestination"),
  indices = arrayOf(Index(value = arrayOf("discussionTopicPk", "discussionTopicDestination",
    "discussionTopicVersionId")),
  Index(value = arrayOf("discussionTopicDestination", "discussionTopicPending")))

)
@Serializable
public class DiscussionTopicReplicate {
  @ReplicationEntityForeignKey
  public var discussionTopicPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var discussionTopicVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var discussionTopicDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var discussionTopicPending: Boolean = true
}
