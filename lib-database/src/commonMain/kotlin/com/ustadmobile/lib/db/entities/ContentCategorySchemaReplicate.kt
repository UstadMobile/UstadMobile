// @Triggers(arrayOf(
//     Trigger(
//         name = "contentcategoryschema_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ContentCategorySchema(contentCategorySchemaUid, schemaName, schemaUrl, contentCategorySchemaLocalChangeSeqNum, contentCategorySchemaMasterChangeSeqNum, contentCategorySchemaLastChangedBy, contentCategorySchemaLct) 
//             VALUES (NEW.contentCategorySchemaUid, NEW.schemaName, NEW.schemaUrl, NEW.contentCategorySchemaLocalChangeSeqNum, NEW.contentCategorySchemaMasterChangeSeqNum, NEW.contentCategorySchemaLastChangedBy, NEW.contentCategorySchemaLct) 
//             /*psql ON CONFLICT (contentCategorySchemaUid) DO UPDATE 
//             SET schemaName = EXCLUDED.schemaName, schemaUrl = EXCLUDED.schemaUrl, contentCategorySchemaLocalChangeSeqNum = EXCLUDED.contentCategorySchemaLocalChangeSeqNum, contentCategorySchemaMasterChangeSeqNum = EXCLUDED.contentCategorySchemaMasterChangeSeqNum, contentCategorySchemaLastChangedBy = EXCLUDED.contentCategorySchemaLastChangedBy, contentCategorySchemaLct = EXCLUDED.contentCategorySchemaLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ContentCategorySchemaReplicate(ccsPk, ccsVersionId, ccsDestination)
//      SELECT ContentCategorySchema.contentCategorySchemaUid AS ccsUid,
//             ContentCategorySchema.contentCategorySchemaLct AS ccsVersionId,
//             :newNodeId AS ccsDestination
//        FROM ContentCategorySchema
//       WHERE ContentCategorySchema.contentCategorySchemaLct != COALESCE(
//             (SELECT ccsVersionId
//                FROM ContentCategorySchemaReplicate
//               WHERE ccsPk = ContentCategorySchema.contentCategorySchemaUid
//                 AND ccsDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(ccsPk, ccsDestination) DO UPDATE
//             SET ccsPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ContentCategorySchema::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ContentCategorySchemaReplicate(ccsPk, ccsVersionId, ccsDestination)
//  SELECT ContentCategorySchema.contentCategorySchemaUid AS ccsUid,
//         ContentCategorySchema.contentCategorySchemaLct AS ccsVersionId,
//         UserSession.usClientNodeId AS ccsDestination
//    FROM ChangeLog
//         JOIN ContentCategorySchema
//             ON ChangeLog.chTableId = 2
//                AND ChangeLog.chEntityPk = ContentCategorySchema.contentCategorySchemaUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ContentCategorySchema.contentCategorySchemaLct != COALESCE(
//         (SELECT ccsVersionId
//            FROM ContentCategorySchemaReplicate
//           WHERE ccsPk = ContentCategorySchema.contentCategorySchemaUid
//             AND ccsDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(ccsPk, ccsDestination) DO UPDATE
//     SET ccsPending = true
//  */               
// """)
// @ReplicationRunOnChange([ContentCategorySchema::class])
// @ReplicationCheckPendingNotificationsFor([ContentCategorySchema::class])
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
  primaryKeys = arrayOf("ccsPk", "ccsDestination"),
  indices = arrayOf(Index(value = arrayOf("ccsPk", "ccsDestination", "ccsVersionId")),
  Index(value = arrayOf("ccsDestination", "ccsPending")))

)
@Serializable
public class ContentCategorySchemaReplicate {
  @ReplicationEntityForeignKey
  public var ccsPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var ccsVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ccsDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var ccsPending: Boolean = true
}
