// @Triggers(arrayOf(
//     Trigger(name = "role_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO Role(roleUid, roleName, roleActive, roleMasterCsn, roleLocalCsn, roleLastChangedBy, roleLct, rolePermissions) VALUES (NEW.roleUid, NEW.roleName, NEW.roleActive, NEW.roleMasterCsn, NEW.roleLocalCsn, NEW.roleLastChangedBy, NEW.roleLct, NEW.rolePermissions) " +
//             "/*psql ON CONFLICT (roleUid) DO UPDATE SET roleName = EXCLUDED.roleName, roleActive = EXCLUDED.roleActive, roleMasterCsn = EXCLUDED.roleMasterCsn, roleLocalCsn = EXCLUDED.roleLocalCsn, roleLastChangedBy = EXCLUDED.roleLastChangedBy, roleLct = EXCLUDED.roleLct, rolePermissions = EXCLUDED.rolePermissions*/"
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
  primaryKeys = arrayOf("rolePk", "roleDestination"),
  indices = arrayOf(Index(value = arrayOf("roleDestination", "roleProcessed", "rolePk")))
)
@Serializable
public class RoleReplicate {
  @ReplicationEntityForeignKey
  public var rolePk: Long = 0

  @ReplicationVersionId
  public var roleVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var roleDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var roleProcessed: Boolean = false
}
