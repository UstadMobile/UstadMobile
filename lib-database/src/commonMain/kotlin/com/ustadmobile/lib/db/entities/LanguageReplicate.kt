// @Triggers(arrayOf(
//     Trigger(
//         name = "language_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO Language(langUid, name, iso_639_1_standard, iso_639_2_standard, iso_639_3_standard, Language_Type, languageActive, langLocalChangeSeqNum, langMasterChangeSeqNum, langLastChangedBy, langLct) 
//             VALUES (NEW.langUid, NEW.name, NEW.iso_639_1_standard, NEW.iso_639_2_standard, NEW.iso_639_3_standard, NEW.Language_Type, NEW.languageActive, NEW.langLocalChangeSeqNum, NEW.langMasterChangeSeqNum, NEW.langLastChangedBy, NEW.langLct) 
//             /*psql ON CONFLICT (langUid) DO UPDATE 
//             SET name = EXCLUDED.name, iso_639_1_standard = EXCLUDED.iso_639_1_standard, iso_639_2_standard = EXCLUDED.iso_639_2_standard, iso_639_3_standard = EXCLUDED.iso_639_3_standard, Language_Type = EXCLUDED.Language_Type, languageActive = EXCLUDED.languageActive, langLocalChangeSeqNum = EXCLUDED.langLocalChangeSeqNum, langMasterChangeSeqNum = EXCLUDED.langMasterChangeSeqNum, langLastChangedBy = EXCLUDED.langLastChangedBy, langLct = EXCLUDED.langLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO LanguageReplicate(languagePk, languageVersionId, languageDestination)
//      SELECT Language.langUid AS languageUid,
//             Language.langLct AS languageVersionId,
//             :newNodeId AS languageDestination
//        FROM Language
//       WHERE Language.langLct != COALESCE(
//             (SELECT languageVersionId
//                FROM LanguageReplicate
//               WHERE languagePk = Language.langUid
//                 AND languageDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(languagePk, languageDestination) DO UPDATE
//             SET languagePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([Language::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO LanguageReplicate(languagePk, languageVersionId, languageDestination)
//  SELECT Language.langUid AS languageUid,
//         Language.langLct AS languageVersionId,
//         UserSession.usClientNodeId AS languageDestination
//    FROM ChangeLog
//         JOIN Language
//             ON ChangeLog.chTableId = 13
//                AND ChangeLog.chEntityPk = Language.langUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND Language.langLct != COALESCE(
//         (SELECT languageVersionId
//            FROM LanguageReplicate
//           WHERE languagePk = Language.langUid
//             AND languageDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(languagePk, languageDestination) DO UPDATE
//     SET languagePending = true
//  */               
// """)
// @ReplicationRunOnChange([Language::class])
// @ReplicationCheckPendingNotificationsFor([Language::class])
// abstract suspend fun replicateOnChange()
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
  primaryKeys = arrayOf("languagePk", "languageDestination"),
  indices = arrayOf(Index(value = arrayOf("languagePk", "languageDestination",
      "languageVersionId")),
  Index(value = arrayOf("languageDestination", "languagePending")))

)
@Serializable
public class LanguageReplicate {
  @ReplicationEntityForeignKey
  public var languagePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var languageVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var languageDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var languagePending: Boolean = true
}
