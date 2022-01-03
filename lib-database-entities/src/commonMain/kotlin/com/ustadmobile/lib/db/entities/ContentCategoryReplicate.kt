// @Triggers(arrayOf(
//     Trigger(
//         name = "contentcategory_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ContentCategory(contentCategoryUid, ctnCatContentCategorySchemaUid, name, contentCategoryLocalChangeSeqNum, contentCategoryMasterChangeSeqNum, contentCategoryLastChangedBy, contentCategoryLct) 
//             VALUES (NEW.contentCategoryUid, NEW.ctnCatContentCategorySchemaUid, NEW.name, NEW.contentCategoryLocalChangeSeqNum, NEW.contentCategoryMasterChangeSeqNum, NEW.contentCategoryLastChangedBy, NEW.contentCategoryLct) 
//             /*psql ON CONFLICT (contentCategoryUid) DO UPDATE 
//             SET ctnCatContentCategorySchemaUid = EXCLUDED.ctnCatContentCategorySchemaUid, name = EXCLUDED.name, contentCategoryLocalChangeSeqNum = EXCLUDED.contentCategoryLocalChangeSeqNum, contentCategoryMasterChangeSeqNum = EXCLUDED.contentCategoryMasterChangeSeqNum, contentCategoryLastChangedBy = EXCLUDED.contentCategoryLastChangedBy, contentCategoryLct = EXCLUDED.contentCategoryLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ContentCategoryReplicate(ccPk, ccVersionId, ccDestination)
//      SELECT ContentCategory.contentCategoryUid AS ccUid,
//             ContentCategory.contentCategoryLct AS ccVersionId,
//             :newNodeId AS ccDestination
//        FROM ContentCategory
//       WHERE ContentCategory.contentCategoryLct != COALESCE(
//             (SELECT ccVersionId
//                FROM ContentCategoryReplicate
//               WHERE ccPk = ContentCategory.contentCategoryUid
//                 AND ccDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(ccPk, ccDestination) DO UPDATE
//             SET ccPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ContentCategory::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ContentCategoryReplicate(ccPk, ccVersionId, ccDestination)
//  SELECT ContentCategory.contentCategoryUid AS ccUid,
//         ContentCategory.contentCategoryLct AS ccVersionId,
//         UserSession.usClientNodeId AS ccDestination
//    FROM ChangeLog
//         JOIN ContentCategory
//             ON ChangeLog.chTableId = 1
//                AND ChangeLog.chEntityPk = ContentCategory.contentCategoryUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ContentCategory.contentCategoryLct != COALESCE(
//         (SELECT ccVersionId
//            FROM ContentCategoryReplicate
//           WHERE ccPk = ContentCategory.contentCategoryUid
//             AND ccDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(ccPk, ccDestination) DO UPDATE
//     SET ccPending = true
//  */               
// """)
// @ReplicationRunOnChange([ContentCategory::class])
// @ReplicationCheckPendingNotificationsFor([ContentCategory::class])
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
  primaryKeys = arrayOf("ccPk", "ccDestination"),
  indices = arrayOf(Index(value = arrayOf("ccPk", "ccDestination", "ccVersionId")),
  Index(value = arrayOf("ccDestination", "ccPending")))

)
@Serializable
public class ContentCategoryReplicate {
  @ReplicationEntityForeignKey
  public var ccPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var ccVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ccDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var ccPending: Boolean = true
}
