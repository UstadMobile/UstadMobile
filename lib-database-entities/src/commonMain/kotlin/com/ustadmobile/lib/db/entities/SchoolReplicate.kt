// @Triggers(arrayOf(
//     Trigger(name = "school_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO School(schoolUid, schoolName, schoolDesc, schoolAddress, schoolActive, schoolPhoneNumber, schoolGender, schoolHolidayCalendarUid, schoolFeatures, schoolLocationLong, schoolLocationLatt, schoolEmailAddress, schoolTeachersPersonGroupUid, schoolStudentsPersonGroupUid, schoolPendingStudentsPersonGroupUid, schoolCode, schoolMasterChangeSeqNum, schoolLocalChangeSeqNum, schoolLastChangedBy, schoolLct, schoolTimeZone) VALUES (NEW.schoolUid, NEW.schoolName, NEW.schoolDesc, NEW.schoolAddress, NEW.schoolActive, NEW.schoolPhoneNumber, NEW.schoolGender, NEW.schoolHolidayCalendarUid, NEW.schoolFeatures, NEW.schoolLocationLong, NEW.schoolLocationLatt, NEW.schoolEmailAddress, NEW.schoolTeachersPersonGroupUid, NEW.schoolStudentsPersonGroupUid, NEW.schoolPendingStudentsPersonGroupUid, NEW.schoolCode, NEW.schoolMasterChangeSeqNum, NEW.schoolLocalChangeSeqNum, NEW.schoolLastChangedBy, NEW.schoolLct, NEW.schoolTimeZone) " +
//             "/*psql ON CONFLICT (schoolUid) DO UPDATE SET schoolName = EXCLUDED.schoolName, schoolDesc = EXCLUDED.schoolDesc, schoolAddress = EXCLUDED.schoolAddress, schoolActive = EXCLUDED.schoolActive, schoolPhoneNumber = EXCLUDED.schoolPhoneNumber, schoolGender = EXCLUDED.schoolGender, schoolHolidayCalendarUid = EXCLUDED.schoolHolidayCalendarUid, schoolFeatures = EXCLUDED.schoolFeatures, schoolLocationLong = EXCLUDED.schoolLocationLong, schoolLocationLatt = EXCLUDED.schoolLocationLatt, schoolEmailAddress = EXCLUDED.schoolEmailAddress, schoolTeachersPersonGroupUid = EXCLUDED.schoolTeachersPersonGroupUid, schoolStudentsPersonGroupUid = EXCLUDED.schoolStudentsPersonGroupUid, schoolPendingStudentsPersonGroupUid = EXCLUDED.schoolPendingStudentsPersonGroupUid, schoolCode = EXCLUDED.schoolCode, schoolMasterChangeSeqNum = EXCLUDED.schoolMasterChangeSeqNum, schoolLocalChangeSeqNum = EXCLUDED.schoolLocalChangeSeqNum, schoolLastChangedBy = EXCLUDED.schoolLastChangedBy, schoolLct = EXCLUDED.schoolLct, schoolTimeZone = EXCLUDED.schoolTimeZone*/"
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
  primaryKeys = arrayOf("schoolPk", "schoolDestination"),
  indices = arrayOf(Index(value = arrayOf("schoolDestination", "schoolProcessed", "schoolPk")))
)
@Serializable
public class SchoolReplicate {
  @ReplicationEntityForeignKey
  public var schoolPk: Long = 0

  @ReplicationVersionId
  public var schoolVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var schoolDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var schoolProcessed: Boolean = false
}
