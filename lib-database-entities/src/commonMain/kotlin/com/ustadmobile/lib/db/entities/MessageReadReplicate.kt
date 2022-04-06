
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
  primaryKeys = arrayOf("messageReadPk", "messageReadDestination"),
  indices = arrayOf(Index(value = arrayOf("messageReadPk", "messageReadDestination", "messageReadVersionId")),
  Index(value = arrayOf("messageReadDestination", "messageReadPending")))

)
@Serializable
public class MessageReadReplicate {
  @ReplicationEntityForeignKey
  public var messageReadPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var messageReadVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var messageReadDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var messageReadPending: Boolean = true
}
