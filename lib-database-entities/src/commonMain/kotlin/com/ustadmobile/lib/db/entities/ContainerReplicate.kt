// @Triggers(arrayOf(
//     Trigger(
//         name = "container_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO Container(containerUid, cntLocalCsn, cntMasterCsn, cntLastModBy, cntLct, fileSize, containerContentEntryUid, cntLastModified, mimeType, remarks, mobileOptimized, cntNumEntries) 
//             VALUES (NEW.containerUid, NEW.cntLocalCsn, NEW.cntMasterCsn, NEW.cntLastModBy, NEW.cntLct, NEW.fileSize, NEW.containerContentEntryUid, NEW.cntLastModified, NEW.mimeType, NEW.remarks, NEW.mobileOptimized, NEW.cntNumEntries) 
//             /*psql ON CONFLICT (containerUid) DO UPDATE 
//             SET cntLocalCsn = EXCLUDED.cntLocalCsn, cntMasterCsn = EXCLUDED.cntMasterCsn, cntLastModBy = EXCLUDED.cntLastModBy, cntLct = EXCLUDED.cntLct, fileSize = EXCLUDED.fileSize, containerContentEntryUid = EXCLUDED.containerContentEntryUid, cntLastModified = EXCLUDED.cntLastModified, mimeType = EXCLUDED.mimeType, remarks = EXCLUDED.remarks, mobileOptimized = EXCLUDED.mobileOptimized, cntNumEntries = EXCLUDED.cntNumEntries
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ContainerReplicate(containerPk, containerVersionId, containerDestination)
//      SELECT Container.containerUid AS containerUid,
//             Container.cntLct AS containerVersionId,
//             :newNodeId AS containerDestination
//        FROM Container
//       WHERE Container.cntLct != COALESCE(
//             (SELECT containerVersionId
//                FROM ContainerReplicate
//               WHERE containerPk = Container.containerUid
//                 AND containerDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(containerPk, containerDestination) DO UPDATE
//             SET containerPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([Container::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ContainerReplicate(containerPk, containerVersionId, containerDestination)
//  SELECT Container.containerUid AS containerUid,
//         Container.cntLct AS containerVersionId,
//         UserSession.usClientNodeId AS containerDestination
//    FROM ChangeLog
//         JOIN Container
//             ON ChangeLog.chTableId = 51
//                AND ChangeLog.chEntityPk = Container.containerUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND Container.cntLct != COALESCE(
//         (SELECT containerVersionId
//            FROM ContainerReplicate
//           WHERE containerPk = Container.containerUid
//             AND containerDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(containerPk, containerDestination) DO UPDATE
//     SET containerPending = true
//  */               
// """)
// @ReplicationRunOnChange([Container::class])
// @ReplicationCheckPendingNotificationsFor([Container::class])
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
  primaryKeys = arrayOf("containerPk", "containerDestination"),
  indices = arrayOf(Index(value = arrayOf("containerPk", "containerDestination",
      "containerVersionId")),
  Index(value = arrayOf("containerDestination", "containerPending")))

)
@Serializable
public class ContainerReplicate {
  @ReplicationEntityForeignKey
  public var containerPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var containerVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var containerDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var containerPending: Boolean = true
}
