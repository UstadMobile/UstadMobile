// @Triggers(arrayOf(
//     Trigger(
//         name = "school_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO School(schoolUid, schoolName, schoolDesc, schoolAddress, schoolActive, schoolPhoneNumber, schoolGender, schoolHolidayCalendarUid, schoolFeatures, schoolLocationLong, schoolLocationLatt, schoolEmailAddress, schoolTeachersPersonGroupUid, schoolStudentsPersonGroupUid, schoolPendingStudentsPersonGroupUid, schoolCode, schoolMasterChangeSeqNum, schoolLocalChangeSeqNum, schoolLastChangedBy, schoolLct, schoolTimeZone) 
//             VALUES (NEW.schoolUid, NEW.schoolName, NEW.schoolDesc, NEW.schoolAddress, NEW.schoolActive, NEW.schoolPhoneNumber, NEW.schoolGender, NEW.schoolHolidayCalendarUid, NEW.schoolFeatures, NEW.schoolLocationLong, NEW.schoolLocationLatt, NEW.schoolEmailAddress, NEW.schoolTeachersPersonGroupUid, NEW.schoolStudentsPersonGroupUid, NEW.schoolPendingStudentsPersonGroupUid, NEW.schoolCode, NEW.schoolMasterChangeSeqNum, NEW.schoolLocalChangeSeqNum, NEW.schoolLastChangedBy, NEW.schoolLct, NEW.schoolTimeZone) 
//             /*psql ON CONFLICT (schoolUid) DO UPDATE 
//             SET schoolName = EXCLUDED.schoolName, schoolDesc = EXCLUDED.schoolDesc, schoolAddress = EXCLUDED.schoolAddress, schoolActive = EXCLUDED.schoolActive, schoolPhoneNumber = EXCLUDED.schoolPhoneNumber, schoolGender = EXCLUDED.schoolGender, schoolHolidayCalendarUid = EXCLUDED.schoolHolidayCalendarUid, schoolFeatures = EXCLUDED.schoolFeatures, schoolLocationLong = EXCLUDED.schoolLocationLong, schoolLocationLatt = EXCLUDED.schoolLocationLatt, schoolEmailAddress = EXCLUDED.schoolEmailAddress, schoolTeachersPersonGroupUid = EXCLUDED.schoolTeachersPersonGroupUid, schoolStudentsPersonGroupUid = EXCLUDED.schoolStudentsPersonGroupUid, schoolPendingStudentsPersonGroupUid = EXCLUDED.schoolPendingStudentsPersonGroupUid, schoolCode = EXCLUDED.schoolCode, schoolMasterChangeSeqNum = EXCLUDED.schoolMasterChangeSeqNum, schoolLocalChangeSeqNum = EXCLUDED.schoolLocalChangeSeqNum, schoolLastChangedBy = EXCLUDED.schoolLastChangedBy, schoolLct = EXCLUDED.schoolLct, schoolTimeZone = EXCLUDED.schoolTimeZone
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO SchoolReplicate(schoolPk, schoolVersionId, schoolDestination)
//      SELECT School.schoolUid AS schoolUid,
//             School.schoolLct AS schoolVersionId,
//             :newNodeId AS schoolDestination
//        FROM School
//       WHERE School.schoolLct != COALESCE(
//             (SELECT schoolVersionId
//                FROM SchoolReplicate
//               WHERE schoolPk = School.schoolUid
//                 AND schoolDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(schoolPk, schoolDestination) DO UPDATE
//             SET schoolPending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([School::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO SchoolReplicate(schoolPk, schoolVersionId, schoolDestination)
//  SELECT School.schoolUid AS schoolUid,
//         School.schoolLct AS schoolVersionId,
//         UserSession.usClientNodeId AS schoolDestination
//    FROM ChangeLog
//         JOIN School
//             ON ChangeLog.chTableId = 164
//                AND ChangeLog.chEntityPk = School.schoolUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND School.schoolLct != COALESCE(
//         (SELECT schoolVersionId
//            FROM SchoolReplicate
//           WHERE schoolPk = School.schoolUid
//             AND schoolDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(schoolPk, schoolDestination) DO UPDATE
//     SET schoolPending = true
//  */               
// """)
// @ReplicationRunOnChange([School::class])
// @ReplicationCheckPendingNotificationsFor([School::class])
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
  primaryKeys = arrayOf("schoolPk", "schoolDestination"),
  indices = arrayOf(Index(value = arrayOf("schoolPk", "schoolDestination", "schoolVersionId")),
  Index(value = arrayOf("schoolDestination", "schoolPending")))

)
@Serializable
public class SchoolReplicate {
  @ReplicationEntityForeignKey
  public var schoolPk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var schoolVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var schoolDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var schoolPending: Boolean = true
}
