// @Triggers(arrayOf(
//     Trigger(
//         name = "languagevariant_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO LanguageVariant(langVariantUid, langUid, countryCode, name, langVariantLocalChangeSeqNum, langVariantMasterChangeSeqNum, langVariantLastChangedBy, langVariantLct) 
//             VALUES (NEW.langVariantUid, NEW.langUid, NEW.countryCode, NEW.name, NEW.langVariantLocalChangeSeqNum, NEW.langVariantMasterChangeSeqNum, NEW.langVariantLastChangedBy, NEW.langVariantLct) 
//             /*psql ON CONFLICT (langVariantUid) DO UPDATE 
//             SET langUid = EXCLUDED.langUid, countryCode = EXCLUDED.countryCode, name = EXCLUDED.name, langVariantLocalChangeSeqNum = EXCLUDED.langVariantLocalChangeSeqNum, langVariantMasterChangeSeqNum = EXCLUDED.langVariantMasterChangeSeqNum, langVariantLastChangedBy = EXCLUDED.langVariantLastChangedBy, langVariantLct = EXCLUDED.langVariantLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO LanguageVariantReplicate(lvPk, lvVersionId, lvDestination)
//      SELECT LanguageVariant.langVariantUid AS lvUid,
//             LanguageVariant.langVariantLct AS lvVersionId,
//             :newNodeId AS lvDestination
//        FROM LanguageVariant
//       WHERE LanguageVariant.langVariantLct != COALESCE(
//             (SELECT lvVersionId
//                FROM LanguageVariantReplicate
//               WHERE lvPk = LanguageVariant.langVariantUid
//                 AND lvDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(lvPk, lvDestination) DO UPDATE
//             SET lvPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([LanguageVariant::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO LanguageVariantReplicate(lvPk, lvVersionId, lvDestination)
//  SELECT LanguageVariant.langVariantUid AS lvUid,
//         LanguageVariant.langVariantLct AS lvVersionId,
//         UserSession.usClientNodeId AS lvDestination
//    FROM ChangeLog
//         JOIN LanguageVariant
//             ON ChangeLog.chTableId = 10
//                AND ChangeLog.chEntityPk = LanguageVariant.langVariantUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND LanguageVariant.langVariantLct != COALESCE(
//         (SELECT lvVersionId
//            FROM LanguageVariantReplicate
//           WHERE lvPk = LanguageVariant.langVariantUid
//             AND lvDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(lvPk, lvDestination) DO UPDATE
//     SET lvPending = true
//  */               
// """)
// @ReplicationRunOnChange([LanguageVariant::class])
// @ReplicationCheckPendingNotificationsFor([LanguageVariant::class])
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
  primaryKeys = arrayOf("lvPk", "lvDestination"),
  indices = arrayOf(Index(value = arrayOf("lvPk", "lvDestination", "lvVersionId")),
  Index(value = arrayOf("lvDestination", "lvPending")))
)
@Serializable
public class LanguageVariantReplicate {
  @ReplicationEntityForeignKey
  public var lvPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var lvVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var lvDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var lvPending: Boolean = true
}
