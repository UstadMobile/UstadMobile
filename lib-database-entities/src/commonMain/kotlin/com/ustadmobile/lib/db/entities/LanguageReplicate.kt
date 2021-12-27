// @Triggers(arrayOf(
//     Trigger(name = "language_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO Language(langUid, name, iso_639_1_standard, iso_639_2_standard, iso_639_3_standard, Language_Type, languageActive, langLocalChangeSeqNum, langMasterChangeSeqNum, langLastChangedBy, langLct) VALUES (NEW.langUid, NEW.name, NEW.iso_639_1_standard, NEW.iso_639_2_standard, NEW.iso_639_3_standard, NEW.Language_Type, NEW.languageActive, NEW.langLocalChangeSeqNum, NEW.langMasterChangeSeqNum, NEW.langLastChangedBy, NEW.langLct) " +
//             "/*psql ON CONFLICT (langUid) DO UPDATE SET name = EXCLUDED.name, iso_639_1_standard = EXCLUDED.iso_639_1_standard, iso_639_2_standard = EXCLUDED.iso_639_2_standard, iso_639_3_standard = EXCLUDED.iso_639_3_standard, Language_Type = EXCLUDED.Language_Type, languageActive = EXCLUDED.languageActive, langLocalChangeSeqNum = EXCLUDED.langLocalChangeSeqNum, langMasterChangeSeqNum = EXCLUDED.langMasterChangeSeqNum, langLastChangedBy = EXCLUDED.langLastChangedBy, langLct = EXCLUDED.langLct*/"
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
  primaryKeys = arrayOf("languagePk", "languageDestination"),
  indices = arrayOf(Index(value = arrayOf("languageDestination", "languageProcessed",
      "languagePk")))
)
@Serializable
public class LanguageReplicate {
  @ReplicationEntityForeignKey
  public var languagePk: Long = 0

  @ReplicationVersionId
  public var languageVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var languageDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var languageProcessed: Boolean = false
}
