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
  primaryKeys = arrayOf("languageFk", "languageDestination"),
  indices = arrayOf(Index(value = arrayOf("languageDestination", "languageProcessed",
      "languageFk")))
)
@Serializable
public class LanguageTracker {
  @ReplicationEntityForeignKey
  public var languageFk: Long = 0

  @ReplicationVersionId
  public var languageVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var languageDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var languageProcessed: Boolean = false
}
