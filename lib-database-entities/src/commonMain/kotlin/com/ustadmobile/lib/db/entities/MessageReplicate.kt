
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
  primaryKeys = arrayOf("messagePk", "messageDestination"),
  indices = arrayOf(Index(value = arrayOf("messagePk", "messageDestination", "messageVersionId")),
  Index(value = arrayOf("messageDestination", "messagePending")))

)
@Serializable
public class MessageReplicate {
  @ReplicationEntityForeignKey
  public var messagePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var messageVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var messageDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var messagePending: Boolean = true
}
