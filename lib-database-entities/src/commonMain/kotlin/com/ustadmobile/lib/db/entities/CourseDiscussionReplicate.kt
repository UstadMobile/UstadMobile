
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
  primaryKeys = arrayOf("courseDiscussionPk", "courseDiscussionDestination"),
  indices = arrayOf(Index(value = arrayOf("courseDiscussionPk", "courseDiscussionDestination",
    "courseDiscussionVersionId")),
  Index(value = arrayOf("courseDiscussionDestination", "courseDiscussionPending")))

)
@Serializable
public class CourseDiscussionReplicate {
  @ReplicationEntityForeignKey
  public var courseDiscussionPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var courseDiscussionVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var courseDiscussionDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var courseDiscussionPending: Boolean = true
}
