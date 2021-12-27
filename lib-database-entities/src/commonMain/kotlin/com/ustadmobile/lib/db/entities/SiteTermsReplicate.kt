// @Triggers(arrayOf(
//     Trigger(name = "siteterms_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO SiteTerms(sTermsUid, termsHtml, sTermsLang, sTermsLangUid, sTermsActive, sTermsLastChangedBy, sTermsPrimaryCsn, sTermsLocalCsn, sTermsLct) VALUES (NEW.sTermsUid, NEW.termsHtml, NEW.sTermsLang, NEW.sTermsLangUid, NEW.sTermsActive, NEW.sTermsLastChangedBy, NEW.sTermsPrimaryCsn, NEW.sTermsLocalCsn, NEW.sTermsLct) " +
//             "/*psql ON CONFLICT (sTermsUid) DO UPDATE SET termsHtml = EXCLUDED.termsHtml, sTermsLang = EXCLUDED.sTermsLang, sTermsLangUid = EXCLUDED.sTermsLangUid, sTermsActive = EXCLUDED.sTermsActive, sTermsLastChangedBy = EXCLUDED.sTermsLastChangedBy, sTermsPrimaryCsn = EXCLUDED.sTermsPrimaryCsn, sTermsLocalCsn = EXCLUDED.sTermsLocalCsn, sTermsLct = EXCLUDED.sTermsLct*/"
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
  primaryKeys = arrayOf("stPk", "stDestination"),
  indices = arrayOf(Index(value = arrayOf("stDestination", "stProcessed", "stPk")))
)
@Serializable
public class SiteTermsReplicate {
  @ReplicationEntityForeignKey
  public var stPk: Long = 0

  @ReplicationVersionId
  public var stVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var stDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var stProcessed: Boolean = false
}
