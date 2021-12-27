// @Triggers(arrayOf(
//     Trigger(name = "xlangmapentry_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO XLangMapEntry(verbLangMapUid, objectLangMapUid, languageLangMapUid, languageVariantLangMapUid, valueLangMap, statementLangMapMasterCsn, statementLangMapLocalCsn, statementLangMapLcb, statementLangMapLct, statementLangMapUid) VALUES (NEW.verbLangMapUid, NEW.objectLangMapUid, NEW.languageLangMapUid, NEW.languageVariantLangMapUid, NEW.valueLangMap, NEW.statementLangMapMasterCsn, NEW.statementLangMapLocalCsn, NEW.statementLangMapLcb, NEW.statementLangMapLct, NEW.statementLangMapUid) " +
//             "/*psql ON CONFLICT (statementLangMapUid) DO UPDATE SET verbLangMapUid = EXCLUDED.verbLangMapUid, objectLangMapUid = EXCLUDED.objectLangMapUid, languageLangMapUid = EXCLUDED.languageLangMapUid, languageVariantLangMapUid = EXCLUDED.languageVariantLangMapUid, valueLangMap = EXCLUDED.valueLangMap, statementLangMapMasterCsn = EXCLUDED.statementLangMapMasterCsn, statementLangMapLocalCsn = EXCLUDED.statementLangMapLocalCsn, statementLangMapLcb = EXCLUDED.statementLangMapLcb, statementLangMapLct = EXCLUDED.statementLangMapLct*/"
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
  primaryKeys = arrayOf("xlmePk", "xlmeDestination"),
  indices = arrayOf(Index(value = arrayOf("xlmeDestination", "xlmeProcessed", "xlmePk")))
)
@Serializable
public class XLangMapEntryReplicate {
  @ReplicationEntityForeignKey
  public var xlmePk: Long = 0

  @ReplicationVersionId
  public var xlmeVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var xlmeDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var xlmeProcessed: Boolean = false
}
