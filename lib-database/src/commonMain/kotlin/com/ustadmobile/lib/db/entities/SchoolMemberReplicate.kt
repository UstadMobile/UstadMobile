// @Triggers(arrayOf(
//     Trigger(
//         name = "schoolmember_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO SchoolMember(schoolMemberUid, schoolMemberPersonUid, schoolMemberSchoolUid, schoolMemberJoinDate, schoolMemberLeftDate, schoolMemberRole, schoolMemberActive, schoolMemberLocalChangeSeqNum, schoolMemberMasterChangeSeqNum, schoolMemberLastChangedBy, schoolMemberLct) 
//             VALUES (NEW.schoolMemberUid, NEW.schoolMemberPersonUid, NEW.schoolMemberSchoolUid, NEW.schoolMemberJoinDate, NEW.schoolMemberLeftDate, NEW.schoolMemberRole, NEW.schoolMemberActive, NEW.schoolMemberLocalChangeSeqNum, NEW.schoolMemberMasterChangeSeqNum, NEW.schoolMemberLastChangedBy, NEW.schoolMemberLct) 
//             /*psql ON CONFLICT (schoolMemberUid) DO UPDATE 
//             SET schoolMemberPersonUid = EXCLUDED.schoolMemberPersonUid, schoolMemberSchoolUid = EXCLUDED.schoolMemberSchoolUid, schoolMemberJoinDate = EXCLUDED.schoolMemberJoinDate, schoolMemberLeftDate = EXCLUDED.schoolMemberLeftDate, schoolMemberRole = EXCLUDED.schoolMemberRole, schoolMemberActive = EXCLUDED.schoolMemberActive, schoolMemberLocalChangeSeqNum = EXCLUDED.schoolMemberLocalChangeSeqNum, schoolMemberMasterChangeSeqNum = EXCLUDED.schoolMemberMasterChangeSeqNum, schoolMemberLastChangedBy = EXCLUDED.schoolMemberLastChangedBy, schoolMemberLct = EXCLUDED.schoolMemberLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO SchoolMemberReplicate(smPk, smVersionId, smDestination)
//      SELECT SchoolMember.schoolMemberUid AS smUid,
//             SchoolMember.schoolMemberLct AS smVersionId,
//             :newNodeId AS smDestination
//        FROM SchoolMember
//       WHERE SchoolMember.schoolMemberLct != COALESCE(
//             (SELECT smVersionId
//                FROM SchoolMemberReplicate
//               WHERE smPk = SchoolMember.schoolMemberUid
//                 AND smDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(smPk, smDestination) DO UPDATE
//             SET smPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([SchoolMember::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO SchoolMemberReplicate(smPk, smVersionId, smDestination)
//  SELECT SchoolMember.schoolMemberUid AS smUid,
//         SchoolMember.schoolMemberLct AS smVersionId,
//         UserSession.usClientNodeId AS smDestination
//    FROM ChangeLog
//         JOIN SchoolMember
//             ON ChangeLog.chTableId = 200
//                AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND SchoolMember.schoolMemberLct != COALESCE(
//         (SELECT smVersionId
//            FROM SchoolMemberReplicate
//           WHERE smPk = SchoolMember.schoolMemberUid
//             AND smDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(smPk, smDestination) DO UPDATE
//     SET smPending = true
//  */               
// """)
// @ReplicationRunOnChange([SchoolMember::class])
// @ReplicationCheckPendingNotificationsFor([SchoolMember::class])
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
  primaryKeys = arrayOf("smPk", "smDestination"),
  indices = arrayOf(Index(value = arrayOf("smPk", "smDestination", "smVersionId")),
  Index(value = arrayOf("smDestination", "smPending")))

)
@Serializable
public class SchoolMemberReplicate {
  @ReplicationEntityForeignKey
  public var smPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var smVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var smDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var smPending: Boolean = true
}
