// @Triggers(arrayOf(
//     Trigger(
//         name = "contententry_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ContentEntry(contentEntryUid, title, description, entryId, author, publisher, licenseType, licenseName, licenseUrl, sourceUrl, thumbnailUrl, lastModified, primaryLanguageUid, languageVariantUid, contentFlags, leaf, publik, ceInactive, completionCriteria, minScore, contentTypeFlag, contentOwner, contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum, contentEntryLastChangedBy, contentEntryLct) 
//             VALUES (NEW.contentEntryUid, NEW.title, NEW.description, NEW.entryId, NEW.author, NEW.publisher, NEW.licenseType, NEW.licenseName, NEW.licenseUrl, NEW.sourceUrl, NEW.thumbnailUrl, NEW.lastModified, NEW.primaryLanguageUid, NEW.languageVariantUid, NEW.contentFlags, NEW.leaf, NEW.publik, NEW.ceInactive, NEW.completionCriteria, NEW.minScore, NEW.contentTypeFlag, NEW.contentOwner, NEW.contentEntryLocalChangeSeqNum, NEW.contentEntryMasterChangeSeqNum, NEW.contentEntryLastChangedBy, NEW.contentEntryLct) 
//             /*psql ON CONFLICT (contentEntryUid) DO UPDATE 
//             SET title = EXCLUDED.title, description = EXCLUDED.description, entryId = EXCLUDED.entryId, author = EXCLUDED.author, publisher = EXCLUDED.publisher, licenseType = EXCLUDED.licenseType, licenseName = EXCLUDED.licenseName, licenseUrl = EXCLUDED.licenseUrl, sourceUrl = EXCLUDED.sourceUrl, thumbnailUrl = EXCLUDED.thumbnailUrl, lastModified = EXCLUDED.lastModified, primaryLanguageUid = EXCLUDED.primaryLanguageUid, languageVariantUid = EXCLUDED.languageVariantUid, contentFlags = EXCLUDED.contentFlags, leaf = EXCLUDED.leaf, publik = EXCLUDED.publik, ceInactive = EXCLUDED.ceInactive, completionCriteria = EXCLUDED.completionCriteria, minScore = EXCLUDED.minScore, contentTypeFlag = EXCLUDED.contentTypeFlag, contentOwner = EXCLUDED.contentOwner, contentEntryLocalChangeSeqNum = EXCLUDED.contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum = EXCLUDED.contentEntryMasterChangeSeqNum, contentEntryLastChangedBy = EXCLUDED.contentEntryLastChangedBy, contentEntryLct = EXCLUDED.contentEntryLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ContentEntryReplicate(cePk, ceVersionId, ceDestination)
//      SELECT ContentEntry.contentEntryUid AS ceUid,
//             ContentEntry.contentEntryLct AS ceVersionId,
//             :newNodeId AS ceDestination
//        FROM ContentEntry
//       WHERE ContentEntry.contentEntryLct != COALESCE(
//             (SELECT ceVersionId
//                FROM ContentEntryReplicate
//               WHERE cePk = ContentEntry.contentEntryUid
//                 AND ceDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(cePk, ceDestination) DO UPDATE
//             SET cePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ContentEntry::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ContentEntryReplicate(cePk, ceVersionId, ceDestination)
//  SELECT ContentEntry.contentEntryUid AS ceUid,
//         ContentEntry.contentEntryLct AS ceVersionId,
//         UserSession.usClientNodeId AS ceDestination
//    FROM ChangeLog
//         JOIN ContentEntry
//             ON ChangeLog.chTableId = 42
//                AND ChangeLog.chEntityPk = ContentEntry.contentEntryUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ContentEntry.contentEntryLct != COALESCE(
//         (SELECT ceVersionId
//            FROM ContentEntryReplicate
//           WHERE cePk = ContentEntry.contentEntryUid
//             AND ceDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(cePk, ceDestination) DO UPDATE
//     SET cePending = true
//  */               
// """)
// @ReplicationRunOnChange([ContentEntry::class])
// @ReplicationCheckPendingNotificationsFor([ContentEntry::class])
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
  primaryKeys = arrayOf("cePk", "ceDestination"),
  indices = arrayOf(Index(value = arrayOf("cePk", "ceDestination", "ceVersionId")),
  Index(value = arrayOf("ceDestination", "cePending")))

)
@Serializable
public class ContentEntryReplicate {
  @ReplicationEntityForeignKey
  public var cePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var ceVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ceDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var cePending: Boolean = true
}
