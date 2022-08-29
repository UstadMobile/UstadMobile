// @Triggers(arrayOf(
//     Trigger(
//         name = "clazzlog_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO ClazzLog(clazzLogUid, clazzLogClazzUid, logDate, timeRecorded, clazzLogDone, cancellationNote, clazzLogCancelled, clazzLogNumPresent, clazzLogNumAbsent, clazzLogNumPartial, clazzLogScheduleUid, clazzLogStatusFlag, clazzLogMSQN, clazzLogLCSN, clazzLogLCB, clazzLogLastChangedTime) 
//             VALUES (NEW.clazzLogUid, NEW.clazzLogClazzUid, NEW.logDate, NEW.timeRecorded, NEW.clazzLogDone, NEW.cancellationNote, NEW.clazzLogCancelled, NEW.clazzLogNumPresent, NEW.clazzLogNumAbsent, NEW.clazzLogNumPartial, NEW.clazzLogScheduleUid, NEW.clazzLogStatusFlag, NEW.clazzLogMSQN, NEW.clazzLogLCSN, NEW.clazzLogLCB, NEW.clazzLogLastChangedTime) 
//             /*psql ON CONFLICT (clazzLogUid) DO UPDATE 
//             SET clazzLogClazzUid = EXCLUDED.clazzLogClazzUid, logDate = EXCLUDED.logDate, timeRecorded = EXCLUDED.timeRecorded, clazzLogDone = EXCLUDED.clazzLogDone, cancellationNote = EXCLUDED.cancellationNote, clazzLogCancelled = EXCLUDED.clazzLogCancelled, clazzLogNumPresent = EXCLUDED.clazzLogNumPresent, clazzLogNumAbsent = EXCLUDED.clazzLogNumAbsent, clazzLogNumPartial = EXCLUDED.clazzLogNumPartial, clazzLogScheduleUid = EXCLUDED.clazzLogScheduleUid, clazzLogStatusFlag = EXCLUDED.clazzLogStatusFlag, clazzLogMSQN = EXCLUDED.clazzLogMSQN, clazzLogLCSN = EXCLUDED.clazzLogLCSN, clazzLogLCB = EXCLUDED.clazzLogLCB, clazzLogLastChangedTime = EXCLUDED.clazzLogLastChangedTime
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ClazzLogReplicate(clPk, clVersionId, clDestination)
//      SELECT ClazzLog.clazzLogUid AS clUid,
//             ClazzLog.clazzLogLastChangedTime AS clVersionId,
//             :newNodeId AS clDestination
//        FROM ClazzLog
//       WHERE ClazzLog.clazzLogLastChangedTime != COALESCE(
//             (SELECT clVersionId
//                FROM ClazzLogReplicate
//               WHERE clPk = ClazzLog.clazzLogUid
//                 AND clDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(clPk, clDestination) DO UPDATE
//             SET clPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([ClazzLog::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ClazzLogReplicate(clPk, clVersionId, clDestination)
//  SELECT ClazzLog.clazzLogUid AS clUid,
//         ClazzLog.clazzLogLastChangedTime AS clVersionId,
//         UserSession.usClientNodeId AS clDestination
//    FROM ChangeLog
//         JOIN ClazzLog
//             ON ChangeLog.chTableId = 14
//                AND ChangeLog.chEntityPk = ClazzLog.clazzLogUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND ClazzLog.clazzLogLastChangedTime != COALESCE(
//         (SELECT clVersionId
//            FROM ClazzLogReplicate
//           WHERE clPk = ClazzLog.clazzLogUid
//             AND clDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(clPk, clDestination) DO UPDATE
//     SET clPending = true
//  */               
// """)
// @ReplicationRunOnChange([ClazzLog::class])
// @ReplicationCheckPendingNotificationsFor([ClazzLog::class])
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
  primaryKeys = arrayOf("clPk", "clDestination"),
  indices = arrayOf(Index(value = arrayOf("clPk", "clDestination", "clVersionId")),
  Index(value = arrayOf("clDestination", "clPending")))

)
@Serializable
public class ClazzLogReplicate {
  @ReplicationEntityForeignKey
  public var clPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var clVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var clDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var clPending: Boolean = true
}
