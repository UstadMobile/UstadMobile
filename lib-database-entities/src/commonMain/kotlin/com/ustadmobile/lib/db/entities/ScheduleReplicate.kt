// @Triggers(arrayOf(
//     Trigger(
//         name = "schedule_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO Schedule(scheduleUid, sceduleStartTime, scheduleEndTime, scheduleDay, scheduleMonth, scheduleFrequency, umCalendarUid, scheduleClazzUid, scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum, scheduleLastChangedBy, scheduleLastChangedTime, scheduleActive) 
//             VALUES (NEW.scheduleUid, NEW.sceduleStartTime, NEW.scheduleEndTime, NEW.scheduleDay, NEW.scheduleMonth, NEW.scheduleFrequency, NEW.umCalendarUid, NEW.scheduleClazzUid, NEW.scheduleMasterChangeSeqNum, NEW.scheduleLocalChangeSeqNum, NEW.scheduleLastChangedBy, NEW.scheduleLastChangedTime, NEW.scheduleActive) 
//             /*psql ON CONFLICT (scheduleUid) DO UPDATE 
//             SET sceduleStartTime = EXCLUDED.sceduleStartTime, scheduleEndTime = EXCLUDED.scheduleEndTime, scheduleDay = EXCLUDED.scheduleDay, scheduleMonth = EXCLUDED.scheduleMonth, scheduleFrequency = EXCLUDED.scheduleFrequency, umCalendarUid = EXCLUDED.umCalendarUid, scheduleClazzUid = EXCLUDED.scheduleClazzUid, scheduleMasterChangeSeqNum = EXCLUDED.scheduleMasterChangeSeqNum, scheduleLocalChangeSeqNum = EXCLUDED.scheduleLocalChangeSeqNum, scheduleLastChangedBy = EXCLUDED.scheduleLastChangedBy, scheduleLastChangedTime = EXCLUDED.scheduleLastChangedTime, scheduleActive = EXCLUDED.scheduleActive
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ScheduleReplicate(schedulePk, scheduleVersionId, scheduleDestination)
//      SELECT Schedule.scheduleUid AS scheduleUid,
//             Schedule.scheduleLastChangedTime AS scheduleVersionId,
//             :newNodeId AS scheduleDestination
//        FROM Schedule
//       WHERE Schedule.scheduleLastChangedTime != COALESCE(
//             (SELECT scheduleVersionId
//                FROM ScheduleReplicate
//               WHERE schedulePk = Schedule.scheduleUid
//                 AND scheduleDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(schedulePk, scheduleDestination) DO UPDATE
//             SET schedulePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([Schedule::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ScheduleReplicate(schedulePk, scheduleVersionId, scheduleDestination)
//  SELECT Schedule.scheduleUid AS scheduleUid,
//         Schedule.scheduleLastChangedTime AS scheduleVersionId,
//         UserSession.usClientNodeId AS scheduleDestination
//    FROM ChangeLog
//         JOIN Schedule
//             ON ChangeLog.chTableId = 21
//                AND ChangeLog.chEntityPk = Schedule.scheduleUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND Schedule.scheduleLastChangedTime != COALESCE(
//         (SELECT scheduleVersionId
//            FROM ScheduleReplicate
//           WHERE schedulePk = Schedule.scheduleUid
//             AND scheduleDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(schedulePk, scheduleDestination) DO UPDATE
//     SET schedulePending = true
//  */               
// """)
// @ReplicationRunOnChange([Schedule::class])
// @ReplicationCheckPendingNotificationsFor([Schedule::class])
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
  primaryKeys = arrayOf("schedulePk", "scheduleDestination"),
  indices = arrayOf(Index(value = arrayOf("schedulePk", "scheduleDestination",
      "scheduleVersionId")),
  Index(value = arrayOf("scheduleDestination", "schedulePending")))

)
@Serializable
public class ScheduleReplicate {
  @ReplicationEntityForeignKey
  public var schedulePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var scheduleVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var scheduleDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var schedulePending: Boolean = true
}
