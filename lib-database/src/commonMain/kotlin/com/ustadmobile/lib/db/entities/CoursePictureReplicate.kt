
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
  primaryKeys = arrayOf("cpPk", "cpDestination"),
  indices = arrayOf(Index(value = arrayOf("cpPk", "cpDestination", "cpVersionId")),
  Index(value = arrayOf("cpDestination", "cpPending")))

)
@Serializable
public class CoursePictureReplicate {
  @ReplicationEntityForeignKey
  public var cpPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var cpVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var cpDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var cpPending: Boolean = true
}
