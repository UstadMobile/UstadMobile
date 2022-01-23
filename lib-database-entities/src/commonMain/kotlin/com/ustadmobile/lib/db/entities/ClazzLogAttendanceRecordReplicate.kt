// @Triggers(arrayOf(
//     Trigger(
//         name = "clazzlogattendancerecord_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ClazzLogAttendanceRecord(clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordPersonUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy, clazzLogAttendanceRecordLastChangedTime) 
//             VALUES (NEW.clazzLogAttendanceRecordUid, NEW.clazzLogAttendanceRecordClazzLogUid, NEW.clazzLogAttendanceRecordPersonUid, NEW.attendanceStatus, NEW.clazzLogAttendanceRecordMasterChangeSeqNum, NEW.clazzLogAttendanceRecordLocalChangeSeqNum, NEW.clazzLogAttendanceRecordLastChangedBy, NEW.clazzLogAttendanceRecordLastChangedTime) 
//             /*psql ON CONFLICT (clazzLogAttendanceRecordUid) DO UPDATE 
//             SET clazzLogAttendanceRecordClazzLogUid = EXCLUDED.clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordPersonUid = EXCLUDED.clazzLogAttendanceRecordPersonUid, attendanceStatus = EXCLUDED.attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum = EXCLUDED.clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum = EXCLUDED.clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy = EXCLUDED.clazzLogAttendanceRecordLastChangedBy, clazzLogAttendanceRecordLastChangedTime = EXCLUDED.clazzLogAttendanceRecordLastChangedTime
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ClazzLogAttendanceRecordReplicate(clarPk, clarVersionId, clarDestination)
//      SELECT ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid AS clarUid,
//             ClazzLogAttendanceRecord.clazzLogAttendanceRecordLastChangedTime AS clarVersionId,
//             :newNodeId AS clarDestination
//        FROM ClazzLogAttendanceRecord
//       WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordLastChangedTime != COALESCE(
//             (SELECT clarVersionId
//                FROM ClazzLogAttendanceRecordReplicate
//               WHERE clarPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid
//                 AND clarDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(clarPk, clarDestination) DO UPDATE
//             SET clarPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ClazzLogAttendanceRecord::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ClazzLogAttendanceRecordReplicate(clarPk, clarVersionId, clarDestination)
//  SELECT ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid AS clarUid,
//         ClazzLogAttendanceRecord.clazzLogAttendanceRecordLastChangedTime AS clarVersionId,
//         UserSession.usClientNodeId AS clarDestination
//    FROM ChangeLog
//         JOIN ClazzLogAttendanceRecord
//             ON ChangeLog.chTableId = 15
//                AND ChangeLog.chEntityPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ClazzLogAttendanceRecord.clazzLogAttendanceRecordLastChangedTime != COALESCE(
//         (SELECT clarVersionId
//            FROM ClazzLogAttendanceRecordReplicate
//           WHERE clarPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid
//             AND clarDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(clarPk, clarDestination) DO UPDATE
//     SET clarPending = true
//  */               
// """)
// @ReplicationRunOnChange([ClazzLogAttendanceRecord::class])
// @ReplicationCheckPendingNotificationsFor([ClazzLogAttendanceRecord::class])
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
  primaryKeys = arrayOf("clarPk", "clarDestination"),
  indices = arrayOf(Index(value = arrayOf("clarPk", "clarDestination", "clarVersionId")),
  Index(value = arrayOf("clarDestination", "clarPending")))

)
@Serializable
public class ClazzLogAttendanceRecordReplicate {
  @ReplicationEntityForeignKey
  public var clarPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var clarVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var clarDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var clarPending: Boolean = true
}
