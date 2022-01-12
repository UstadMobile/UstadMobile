// @Triggers(arrayOf(
//     Trigger(
//         name = "clazz_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO Clazz(clazzUid, clazzName, clazzDesc, attendanceAverage, clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid, isClazzActive, clazzLocationUid, clazzStartTime, clazzEndTime, clazzFeatures, clazzSchoolUid, clazzMasterChangeSeqNum, clazzLocalChangeSeqNum, clazzLastChangedBy, clazzLct, clazzTimeZone, clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid, clazzCode) 
//             VALUES (NEW.clazzUid, NEW.clazzName, NEW.clazzDesc, NEW.attendanceAverage, NEW.clazzHolidayUMCalendarUid, NEW.clazzScheuleUMCalendarUid, NEW.isClazzActive, NEW.clazzLocationUid, NEW.clazzStartTime, NEW.clazzEndTime, NEW.clazzFeatures, NEW.clazzSchoolUid, NEW.clazzMasterChangeSeqNum, NEW.clazzLocalChangeSeqNum, NEW.clazzLastChangedBy, NEW.clazzLct, NEW.clazzTimeZone, NEW.clazzStudentsPersonGroupUid, NEW.clazzTeachersPersonGroupUid, NEW.clazzPendingStudentsPersonGroupUid, NEW.clazzParentsPersonGroupUid, NEW.clazzCode) 
//             /*psql ON CONFLICT (clazzUid) DO UPDATE 
//             SET clazzName = EXCLUDED.clazzName, clazzDesc = EXCLUDED.clazzDesc, attendanceAverage = EXCLUDED.attendanceAverage, clazzHolidayUMCalendarUid = EXCLUDED.clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid = EXCLUDED.clazzScheuleUMCalendarUid, isClazzActive = EXCLUDED.isClazzActive, clazzLocationUid = EXCLUDED.clazzLocationUid, clazzStartTime = EXCLUDED.clazzStartTime, clazzEndTime = EXCLUDED.clazzEndTime, clazzFeatures = EXCLUDED.clazzFeatures, clazzSchoolUid = EXCLUDED.clazzSchoolUid, clazzMasterChangeSeqNum = EXCLUDED.clazzMasterChangeSeqNum, clazzLocalChangeSeqNum = EXCLUDED.clazzLocalChangeSeqNum, clazzLastChangedBy = EXCLUDED.clazzLastChangedBy, clazzLct = EXCLUDED.clazzLct, clazzTimeZone = EXCLUDED.clazzTimeZone, clazzStudentsPersonGroupUid = EXCLUDED.clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid = EXCLUDED.clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid = EXCLUDED.clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid = EXCLUDED.clazzParentsPersonGroupUid, clazzCode = EXCLUDED.clazzCode
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO ClazzReplicate(clazzPk, clazzVersionId, clazzDestination)
//      SELECT Clazz.clazzUid AS clazzUid,
//             Clazz.clazzLct AS clazzVersionId,
//             :newNodeId AS clazzDestination
//        FROM Clazz
//       WHERE Clazz.clazzLct != COALESCE(
//             (SELECT clazzVersionId
//                FROM ClazzReplicate
//               WHERE clazzPk = Clazz.clazzUid
//                 AND clazzDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(clazzPk, clazzDestination) DO UPDATE
//             SET clazzPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([Clazz::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO ClazzReplicate(clazzPk, clazzVersionId, clazzDestination)
//  SELECT Clazz.clazzUid AS clazzUid,
//         Clazz.clazzLct AS clazzVersionId,
//         UserSession.usClientNodeId AS clazzDestination
//    FROM ChangeLog
//         JOIN Clazz
//             ON ChangeLog.chTableId = 6
//                AND ChangeLog.chEntityPk = Clazz.clazzUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND Clazz.clazzLct != COALESCE(
//         (SELECT clazzVersionId
//            FROM ClazzReplicate
//           WHERE clazzPk = Clazz.clazzUid
//             AND clazzDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(clazzPk, clazzDestination) DO UPDATE
//     SET clazzPending = true
//  */               
// """)
// @ReplicationRunOnChange([Clazz::class])
// @ReplicationCheckPendingNotificationsFor([Clazz::class])
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
  primaryKeys = arrayOf("clazzPk", "clazzDestination"),
  indices = arrayOf(Index(value = arrayOf("clazzPk", "clazzDestination", "clazzVersionId")),
  Index(value = arrayOf("clazzDestination", "clazzPending")))

)
@Serializable
public class ClazzReplicate {
  @ReplicationEntityForeignKey
  public var clazzPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var clazzVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var clazzDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var clazzPending: Boolean = true
}
