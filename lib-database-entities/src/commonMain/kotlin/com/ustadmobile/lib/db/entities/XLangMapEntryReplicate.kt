// @Triggers(arrayOf(
//     Trigger(
//         name = "xlangmapentry_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO XLangMapEntry(verbLangMapUid, objectLangMapUid, languageLangMapUid, languageVariantLangMapUid, valueLangMap, statementLangMapMasterCsn, statementLangMapLocalCsn, statementLangMapLcb, statementLangMapLct, statementLangMapUid) 
//             VALUES (NEW.verbLangMapUid, NEW.objectLangMapUid, NEW.languageLangMapUid, NEW.languageVariantLangMapUid, NEW.valueLangMap, NEW.statementLangMapMasterCsn, NEW.statementLangMapLocalCsn, NEW.statementLangMapLcb, NEW.statementLangMapLct, NEW.statementLangMapUid) 
//             /*psql ON CONFLICT (statementLangMapUid) DO UPDATE 
//             SET verbLangMapUid = EXCLUDED.verbLangMapUid, objectLangMapUid = EXCLUDED.objectLangMapUid, languageLangMapUid = EXCLUDED.languageLangMapUid, languageVariantLangMapUid = EXCLUDED.languageVariantLangMapUid, valueLangMap = EXCLUDED.valueLangMap, statementLangMapMasterCsn = EXCLUDED.statementLangMapMasterCsn, statementLangMapLocalCsn = EXCLUDED.statementLangMapLocalCsn, statementLangMapLcb = EXCLUDED.statementLangMapLcb, statementLangMapLct = EXCLUDED.statementLangMapLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO XLangMapEntryReplicate(xlmePk, xlmeVersionId, xlmeDestination)
//      SELECT XLangMapEntry.statementLangMapUid AS xlmeUid,
//             XLangMapEntry.statementLangMapLct AS xlmeVersionId,
//             :newNodeId AS xlmeDestination
//        FROM XLangMapEntry
//       WHERE XLangMapEntry.statementLangMapLct != COALESCE(
//             (SELECT xlmeVersionId
//                FROM XLangMapEntryReplicate
//               WHERE xlmePk = XLangMapEntry.statementLangMapUid
//                 AND xlmeDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(xlmePk, xlmeDestination) DO UPDATE
//             SET xlmePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([XLangMapEntry::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO XLangMapEntryReplicate(xlmePk, xlmeVersionId, xlmeDestination)
//  SELECT XLangMapEntry.statementLangMapUid AS xlmeUid,
//         XLangMapEntry.statementLangMapLct AS xlmeVersionId,
//         UserSession.usClientNodeId AS xlmeDestination
//    FROM ChangeLog
//         JOIN XLangMapEntry
//             ON ChangeLog.chTableId = 74
//                AND ChangeLog.chEntityPk = XLangMapEntry.statementLangMapUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND XLangMapEntry.statementLangMapLct != COALESCE(
//         (SELECT xlmeVersionId
//            FROM XLangMapEntryReplicate
//           WHERE xlmePk = XLangMapEntry.statementLangMapUid
//             AND xlmeDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(xlmePk, xlmeDestination) DO UPDATE
//     SET xlmePending = true
//  */               
// """)
// @ReplicationRunOnChange([XLangMapEntry::class])
// @ReplicationCheckPendingNotificationsFor([XLangMapEntry::class])
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
  primaryKeys = arrayOf("xlmePk", "xlmeDestination"),
  indices = arrayOf(Index(value = arrayOf("xlmePk", "xlmeDestination", "xlmeVersionId")),
  Index(value = arrayOf("xlmeDestination", "xlmePending")))

)
@Serializable
public class XLangMapEntryReplicate {
  @ReplicationEntityForeignKey
  public var xlmePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var xlmeVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var xlmeDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var xlmePending: Boolean = true
}
