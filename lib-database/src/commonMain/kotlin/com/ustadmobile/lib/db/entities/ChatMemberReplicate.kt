
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
  primaryKeys = arrayOf("chatMemberPk", "chatMemberDestination"),
  indices = arrayOf(Index(value = arrayOf("chatMemberPk", "chatMemberDestination", "chatMemberVersionId")),
  Index(value = arrayOf("chatMemberDestination", "chatMemberPending")))

)
@Serializable
public class ChatMemberReplicate {
  @ReplicationEntityForeignKey
  public var chatMemberPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var chatMemberVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var chatMemberDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var chatMemberPending: Boolean = true
}
