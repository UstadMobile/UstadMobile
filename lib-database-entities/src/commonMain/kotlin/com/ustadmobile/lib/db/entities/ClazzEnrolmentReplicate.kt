// @Triggers(arrayOf(
//     Trigger(name = "clazzenrolment_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO ClazzEnrolment(clazzEnrolmentUid, clazzEnrolmentPersonUid, clazzEnrolmentClazzUid, clazzEnrolmentDateJoined, clazzEnrolmentDateLeft, clazzEnrolmentRole, clazzEnrolmentAttendancePercentage, clazzEnrolmentActive, clazzEnrolmentLeavingReasonUid, clazzEnrolmentOutcome, clazzEnrolmentLocalChangeSeqNum, clazzEnrolmentMasterChangeSeqNum, clazzEnrolmentLastChangedBy, clazzEnrolmentLct) VALUES (NEW.clazzEnrolmentUid, NEW.clazzEnrolmentPersonUid, NEW.clazzEnrolmentClazzUid, NEW.clazzEnrolmentDateJoined, NEW.clazzEnrolmentDateLeft, NEW.clazzEnrolmentRole, NEW.clazzEnrolmentAttendancePercentage, NEW.clazzEnrolmentActive, NEW.clazzEnrolmentLeavingReasonUid, NEW.clazzEnrolmentOutcome, NEW.clazzEnrolmentLocalChangeSeqNum, NEW.clazzEnrolmentMasterChangeSeqNum, NEW.clazzEnrolmentLastChangedBy, NEW.clazzEnrolmentLct) " +
//             "/*psql ON CONFLICT (clazzEnrolmentUid) DO UPDATE SET clazzEnrolmentPersonUid = EXCLUDED.clazzEnrolmentPersonUid, clazzEnrolmentClazzUid = EXCLUDED.clazzEnrolmentClazzUid, clazzEnrolmentDateJoined = EXCLUDED.clazzEnrolmentDateJoined, clazzEnrolmentDateLeft = EXCLUDED.clazzEnrolmentDateLeft, clazzEnrolmentRole = EXCLUDED.clazzEnrolmentRole, clazzEnrolmentAttendancePercentage = EXCLUDED.clazzEnrolmentAttendancePercentage, clazzEnrolmentActive = EXCLUDED.clazzEnrolmentActive, clazzEnrolmentLeavingReasonUid = EXCLUDED.clazzEnrolmentLeavingReasonUid, clazzEnrolmentOutcome = EXCLUDED.clazzEnrolmentOutcome, clazzEnrolmentLocalChangeSeqNum = EXCLUDED.clazzEnrolmentLocalChangeSeqNum, clazzEnrolmentMasterChangeSeqNum = EXCLUDED.clazzEnrolmentMasterChangeSeqNum, clazzEnrolmentLastChangedBy = EXCLUDED.clazzEnrolmentLastChangedBy, clazzEnrolmentLct = EXCLUDED.clazzEnrolmentLct*/"
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
  primaryKeys = arrayOf("cePk", "ceDestination"),
  indices = arrayOf(Index(value = arrayOf("ceDestination", "ceProcessed", "cePk")))
)
@Serializable
public class ClazzEnrolmentReplicate {
  @ReplicationEntityForeignKey
  public var cePk: Long = 0

  @ReplicationVersionId
  public var ceVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var ceDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var ceProcessed: Boolean = false
}
