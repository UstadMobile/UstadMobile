// @Triggers(arrayOf(
//     Trigger(
//         name = "clazzenrolment_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ClazzEnrolment(clazzEnrolmentUid, clazzEnrolmentPersonUid, clazzEnrolmentClazzUid, clazzEnrolmentDateJoined, clazzEnrolmentDateLeft, clazzEnrolmentRole, clazzEnrolmentAttendancePercentage, clazzEnrolmentActive, clazzEnrolmentLeavingReasonUid, clazzEnrolmentOutcome, clazzEnrolmentLocalChangeSeqNum, clazzEnrolmentMasterChangeSeqNum, clazzEnrolmentLastChangedBy, clazzEnrolmentLct) 
//             VALUES (NEW.clazzEnrolmentUid, NEW.clazzEnrolmentPersonUid, NEW.clazzEnrolmentClazzUid, NEW.clazzEnrolmentDateJoined, NEW.clazzEnrolmentDateLeft, NEW.clazzEnrolmentRole, NEW.clazzEnrolmentAttendancePercentage, NEW.clazzEnrolmentActive, NEW.clazzEnrolmentLeavingReasonUid, NEW.clazzEnrolmentOutcome, NEW.clazzEnrolmentLocalChangeSeqNum, NEW.clazzEnrolmentMasterChangeSeqNum, NEW.clazzEnrolmentLastChangedBy, NEW.clazzEnrolmentLct) 
//             /*psql ON CONFLICT (clazzEnrolmentUid) DO UPDATE 
//             SET clazzEnrolmentPersonUid = EXCLUDED.clazzEnrolmentPersonUid, clazzEnrolmentClazzUid = EXCLUDED.clazzEnrolmentClazzUid, clazzEnrolmentDateJoined = EXCLUDED.clazzEnrolmentDateJoined, clazzEnrolmentDateLeft = EXCLUDED.clazzEnrolmentDateLeft, clazzEnrolmentRole = EXCLUDED.clazzEnrolmentRole, clazzEnrolmentAttendancePercentage = EXCLUDED.clazzEnrolmentAttendancePercentage, clazzEnrolmentActive = EXCLUDED.clazzEnrolmentActive, clazzEnrolmentLeavingReasonUid = EXCLUDED.clazzEnrolmentLeavingReasonUid, clazzEnrolmentOutcome = EXCLUDED.clazzEnrolmentOutcome, clazzEnrolmentLocalChangeSeqNum = EXCLUDED.clazzEnrolmentLocalChangeSeqNum, clazzEnrolmentMasterChangeSeqNum = EXCLUDED.clazzEnrolmentMasterChangeSeqNum, clazzEnrolmentLastChangedBy = EXCLUDED.clazzEnrolmentLastChangedBy, clazzEnrolmentLct = EXCLUDED.clazzEnrolmentLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ClazzEnrolmentReplicate(cePk, ceVersionId, ceDestination)
//      SELECT ClazzEnrolment.clazzEnrolmentUid AS ceUid,
//             ClazzEnrolment.clazzEnrolmentLct AS ceVersionId,
//             :newNodeId AS ceDestination
//        FROM ClazzEnrolment
//       WHERE ClazzEnrolment.clazzEnrolmentLct != COALESCE(
//             (SELECT ceVersionId
//                FROM ClazzEnrolmentReplicate
//               WHERE cePk = ClazzEnrolment.clazzEnrolmentUid
//                 AND ceDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(cePk, ceDestination) DO UPDATE
//             SET cePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ClazzEnrolment::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ClazzEnrolmentReplicate(cePk, ceVersionId, ceDestination)
//  SELECT ClazzEnrolment.clazzEnrolmentUid AS ceUid,
//         ClazzEnrolment.clazzEnrolmentLct AS ceVersionId,
//         UserSession.usClientNodeId AS ceDestination
//    FROM ChangeLog
//         JOIN ClazzEnrolment
//             ON ChangeLog.chTableId = 65
//                AND ChangeLog.chEntityPk = ClazzEnrolment.clazzEnrolmentUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ClazzEnrolment.clazzEnrolmentLct != COALESCE(
//         (SELECT ceVersionId
//            FROM ClazzEnrolmentReplicate
//           WHERE cePk = ClazzEnrolment.clazzEnrolmentUid
//             AND ceDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(cePk, ceDestination) DO UPDATE
//     SET cePending = true
//  */               
// """)
// @ReplicationRunOnChange([ClazzEnrolment::class])
// @ReplicationCheckPendingNotificationsFor([ClazzEnrolment::class])
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
public class ClazzEnrolmentReplicate {
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
