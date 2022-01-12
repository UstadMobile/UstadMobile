// @Triggers(arrayOf(
//     Trigger(
//         name = "holidaycalendar_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO HolidayCalendar(umCalendarUid, umCalendarName, umCalendarCategory, umCalendarActive, umCalendarMasterChangeSeqNum, umCalendarLocalChangeSeqNum, umCalendarLastChangedBy, umCalendarLct) 
//             VALUES (NEW.umCalendarUid, NEW.umCalendarName, NEW.umCalendarCategory, NEW.umCalendarActive, NEW.umCalendarMasterChangeSeqNum, NEW.umCalendarLocalChangeSeqNum, NEW.umCalendarLastChangedBy, NEW.umCalendarLct) 
//             /*psql ON CONFLICT (umCalendarUid) DO UPDATE 
//             SET umCalendarName = EXCLUDED.umCalendarName, umCalendarCategory = EXCLUDED.umCalendarCategory, umCalendarActive = EXCLUDED.umCalendarActive, umCalendarMasterChangeSeqNum = EXCLUDED.umCalendarMasterChangeSeqNum, umCalendarLocalChangeSeqNum = EXCLUDED.umCalendarLocalChangeSeqNum, umCalendarLastChangedBy = EXCLUDED.umCalendarLastChangedBy, umCalendarLct = EXCLUDED.umCalendarLct
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO HolidayCalendarReplicate(hcPk, hcVersionId, hcDestination)
//      SELECT HolidayCalendar.umCalendarUid AS hcUid,
//             HolidayCalendar.umCalendarLct AS hcVersionId,
//             :newNodeId AS hcDestination
//        FROM HolidayCalendar
//       WHERE HolidayCalendar.umCalendarLct != COALESCE(
//             (SELECT hcVersionId
//                FROM HolidayCalendarReplicate
//               WHERE hcPk = HolidayCalendar.umCalendarUid
//                 AND hcDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(hcPk, hcDestination) DO UPDATE
//             SET hcPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([HolidayCalendar::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO HolidayCalendarReplicate(hcPk, hcVersionId, hcDestination)
//  SELECT HolidayCalendar.umCalendarUid AS hcUid,
//         HolidayCalendar.umCalendarLct AS hcVersionId,
//         UserSession.usClientNodeId AS hcDestination
//    FROM ChangeLog
//         JOIN HolidayCalendar
//             ON ChangeLog.chTableId = 28
//                AND ChangeLog.chEntityPk = HolidayCalendar.umCalendarUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND HolidayCalendar.umCalendarLct != COALESCE(
//         (SELECT hcVersionId
//            FROM HolidayCalendarReplicate
//           WHERE hcPk = HolidayCalendar.umCalendarUid
//             AND hcDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(hcPk, hcDestination) DO UPDATE
//     SET hcPending = true
//  */               
// """)
// @ReplicationRunOnChange([HolidayCalendar::class])
// @ReplicationCheckPendingNotificationsFor([HolidayCalendar::class])
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
  primaryKeys = arrayOf("hcPk", "hcDestination"),
  indices = arrayOf(Index(value = arrayOf("hcPk", "hcDestination", "hcVersionId")),
  Index(value = arrayOf("hcDestination", "hcPending")))

)
@Serializable
public class HolidayCalendarReplicate {
  @ReplicationEntityForeignKey
  public var hcPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var hcVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var hcDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var hcPending: Boolean = true
}
