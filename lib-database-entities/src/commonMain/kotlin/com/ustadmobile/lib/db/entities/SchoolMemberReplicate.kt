// @Triggers(arrayOf(
//     Trigger(name = "schoolmember_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO SchoolMember(schoolMemberUid, schoolMemberPersonUid, schoolMemberSchoolUid, schoolMemberJoinDate, schoolMemberLeftDate, schoolMemberRole, schoolMemberActive, schoolMemberLocalChangeSeqNum, schoolMemberMasterChangeSeqNum, schoolMemberLastChangedBy, schoolMemberLct) VALUES (NEW.schoolMemberUid, NEW.schoolMemberPersonUid, NEW.schoolMemberSchoolUid, NEW.schoolMemberJoinDate, NEW.schoolMemberLeftDate, NEW.schoolMemberRole, NEW.schoolMemberActive, NEW.schoolMemberLocalChangeSeqNum, NEW.schoolMemberMasterChangeSeqNum, NEW.schoolMemberLastChangedBy, NEW.schoolMemberLct) " +
//             "/*psql ON CONFLICT (schoolMemberUid) DO UPDATE SET schoolMemberPersonUid = EXCLUDED.schoolMemberPersonUid, schoolMemberSchoolUid = EXCLUDED.schoolMemberSchoolUid, schoolMemberJoinDate = EXCLUDED.schoolMemberJoinDate, schoolMemberLeftDate = EXCLUDED.schoolMemberLeftDate, schoolMemberRole = EXCLUDED.schoolMemberRole, schoolMemberActive = EXCLUDED.schoolMemberActive, schoolMemberLocalChangeSeqNum = EXCLUDED.schoolMemberLocalChangeSeqNum, schoolMemberMasterChangeSeqNum = EXCLUDED.schoolMemberMasterChangeSeqNum, schoolMemberLastChangedBy = EXCLUDED.schoolMemberLastChangedBy, schoolMemberLct = EXCLUDED.schoolMemberLct*/"
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
  primaryKeys = arrayOf("smPk", "smDestination"),
  indices = arrayOf(Index(value = arrayOf("smDestination", "smProcessed", "smPk")))
)
@Serializable
public class SchoolMemberReplicate {
  @ReplicationEntityForeignKey
  public var smPk: Long = 0

  @ReplicationVersionId
  public var smVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var smDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var smProcessed: Boolean = false
}
