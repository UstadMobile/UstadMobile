// @Triggers(arrayOf(
//     Trigger(
//         name = "role_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO Role(roleUid, roleName, roleActive, roleMasterCsn, roleLocalCsn, roleLastChangedBy, roleLct, rolePermissions) 
//             VALUES (NEW.roleUid, NEW.roleName, NEW.roleActive, NEW.roleMasterCsn, NEW.roleLocalCsn, NEW.roleLastChangedBy, NEW.roleLct, NEW.rolePermissions) 
//             /*psql ON CONFLICT (roleUid) DO UPDATE 
//             SET roleName = EXCLUDED.roleName, roleActive = EXCLUDED.roleActive, roleMasterCsn = EXCLUDED.roleMasterCsn, roleLocalCsn = EXCLUDED.roleLocalCsn, roleLastChangedBy = EXCLUDED.roleLastChangedBy, roleLct = EXCLUDED.roleLct, rolePermissions = EXCLUDED.rolePermissions
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO RoleReplicate(rolePk, roleVersionId, roleDestination)
//      SELECT Role.roleUid AS roleUid,
//             Role.roleLct AS roleVersionId,
//             :newNodeId AS roleDestination
//        FROM Role
//       WHERE Role.roleLct != COALESCE(
//             (SELECT roleVersionId
//                FROM RoleReplicate
//               WHERE rolePk = Role.roleUid
//                 AND roleDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(rolePk, roleDestination) DO UPDATE
//             SET rolePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([Role::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO RoleReplicate(rolePk, roleVersionId, roleDestination)
//  SELECT Role.roleUid AS roleUid,
//         Role.roleLct AS roleVersionId,
//         UserSession.usClientNodeId AS roleDestination
//    FROM ChangeLog
//         JOIN Role
//             ON ChangeLog.chTableId = 45
//                AND ChangeLog.chEntityPk = Role.roleUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND Role.roleLct != COALESCE(
//         (SELECT roleVersionId
//            FROM RoleReplicate
//           WHERE rolePk = Role.roleUid
//             AND roleDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(rolePk, roleDestination) DO UPDATE
//     SET rolePending = true
//  */               
// """)
// @ReplicationRunOnChange([Role::class])
// @ReplicationCheckPendingNotificationsFor([Role::class])
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
  primaryKeys = arrayOf("rolePk", "roleDestination"),
  indices = arrayOf(Index(value = arrayOf("rolePk", "roleDestination", "roleVersionId")),
  Index(value = arrayOf("roleDestination", "rolePending")))

)
@Serializable
public class RoleReplicate {
  @ReplicationEntityForeignKey
  public var rolePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var roleVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var roleDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var rolePending: Boolean = true
}
