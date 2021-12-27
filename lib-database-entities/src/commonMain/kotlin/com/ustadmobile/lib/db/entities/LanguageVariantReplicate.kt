// @Triggers(arrayOf(
//     Trigger(name = "languagevariant_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO LanguageVariant(langVariantUid, langUid, countryCode, name, langVariantLocalChangeSeqNum, langVariantMasterChangeSeqNum, langVariantLastChangedBy, langVariantLct) VALUES (NEW.langVariantUid, NEW.langUid, NEW.countryCode, NEW.name, NEW.langVariantLocalChangeSeqNum, NEW.langVariantMasterChangeSeqNum, NEW.langVariantLastChangedBy, NEW.langVariantLct) " +
//             "/*psql ON CONFLICT (langVariantUid) DO UPDATE SET langUid = EXCLUDED.langUid, countryCode = EXCLUDED.countryCode, name = EXCLUDED.name, langVariantLocalChangeSeqNum = EXCLUDED.langVariantLocalChangeSeqNum, langVariantMasterChangeSeqNum = EXCLUDED.langVariantMasterChangeSeqNum, langVariantLastChangedBy = EXCLUDED.langVariantLastChangedBy, langVariantLct = EXCLUDED.langVariantLct*/"
//             ])
//     )
// )            
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
  primaryKeys = arrayOf("lvPk", "lvDestination"),
  indices = arrayOf(Index(value = arrayOf("lvDestination", "lvProcessed", "lvPk")))
)
@Serializable
public class LanguageVariantReplicate {
  @ReplicationEntityForeignKey
  public var lvPk: Long = 0

  @ReplicationVersionId
  public var lvVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var lvDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var lvProcessed: Boolean = false
}
