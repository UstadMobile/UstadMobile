
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
  primaryKeys = arrayOf("chatPk", "chatDestination"),
  indices = arrayOf(Index(value = arrayOf("chatPk", "chatDestination", "chatVersionId")),
  Index(value = arrayOf("chatDestination", "chatPending")))

)
@Serializable
public class ChatReplicate {
  @ReplicationEntityForeignKey
  public var chatPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var chatVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var chatDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var chatPending: Boolean = true
}
