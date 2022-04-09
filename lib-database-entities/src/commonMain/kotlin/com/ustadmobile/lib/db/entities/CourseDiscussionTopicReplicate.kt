
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
  primaryKeys = arrayOf("courseDiscussionTopicPk", "courseDiscussionTopicDestination"),
  indices = arrayOf(Index(value = arrayOf("courseDiscussionTopicPk", "courseDiscussionTopicDestination",
    "courseDiscussionTopicVersionId")),
  Index(value = arrayOf("courseDiscussionTopicDestination", "courseDiscussionTopicPending")))

)
@Serializable
public class CourseDiscussionTopicReplicate {
  @ReplicationEntityForeignKey
  public var courseDiscussionTopicPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var courseDiscussionTopicVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var courseDiscussionTopicDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var courseDiscussionTopicPending: Boolean = true
}
