// @Triggers(arrayOf(
//     Trigger(
//         name = "holiday_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO Holiday(holUid, holMasterCsn, holLocalCsn, holLastModBy, holLct, holActive, holHolidayCalendarUid, holStartTime, holEndTime, holName) 
//             VALUES (NEW.holUid, NEW.holMasterCsn, NEW.holLocalCsn, NEW.holLastModBy, NEW.holLct, NEW.holActive, NEW.holHolidayCalendarUid, NEW.holStartTime, NEW.holEndTime, NEW.holName) 
//             /*psql ON CONFLICT (holUid) DO UPDATE 
//             SET holMasterCsn = EXCLUDED.holMasterCsn, holLocalCsn = EXCLUDED.holLocalCsn, holLastModBy = EXCLUDED.holLastModBy, holLct = EXCLUDED.holLct, holActive = EXCLUDED.holActive, holHolidayCalendarUid = EXCLUDED.holHolidayCalendarUid, holStartTime = EXCLUDED.holStartTime, holEndTime = EXCLUDED.holEndTime, holName = EXCLUDED.holName
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO HolidayReplicate(holidayPk, holidayVersionId, holidayDestination)
//      SELECT Holiday.holUid AS holidayUid,
//             Holiday.holLct AS holidayVersionId,
//             :newNodeId AS holidayDestination
//        FROM Holiday
//       WHERE Holiday.holLct != COALESCE(
//             (SELECT holidayVersionId
//                FROM HolidayReplicate
//               WHERE holidayPk = Holiday.holUid
//                 AND holidayDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(holidayPk, holidayDestination) DO UPDATE
//             SET holidayPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([Holiday::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO HolidayReplicate(holidayPk, holidayVersionId, holidayDestination)
//  SELECT Holiday.holUid AS holidayUid,
//         Holiday.holLct AS holidayVersionId,
//         UserSession.usClientNodeId AS holidayDestination
//    FROM ChangeLog
//         JOIN Holiday
//             ON ChangeLog.chTableId = 99
//                AND ChangeLog.chEntityPk = Holiday.holUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND Holiday.holLct != COALESCE(
//         (SELECT holidayVersionId
//            FROM HolidayReplicate
//           WHERE holidayPk = Holiday.holUid
//             AND holidayDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(holidayPk, holidayDestination) DO UPDATE
//     SET holidayPending = true
//  */               
// """)
// @ReplicationRunOnChange([Holiday::class])
// @ReplicationCheckPendingNotificationsFor([Holiday::class])
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
  primaryKeys = arrayOf("holidayPk", "holidayDestination"),
  indices = arrayOf(Index(value = arrayOf("holidayPk", "holidayDestination", "holidayVersionId")),
  Index(value = arrayOf("holidayDestination", "holidayPending")))

)
@Serializable
public class HolidayReplicate {
  @ReplicationEntityForeignKey
  public var holidayPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var holidayVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var holidayDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var holidayPending: Boolean = true
}
