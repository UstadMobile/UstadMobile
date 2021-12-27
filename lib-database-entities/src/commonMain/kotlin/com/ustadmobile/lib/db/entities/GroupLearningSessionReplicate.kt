// @Triggers(arrayOf(
//     Trigger(name = "grouplearningsession_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO GroupLearningSession(groupLearningSessionUid, groupLearningSessionContentUid, groupLearningSessionLearnerGroupUid, groupLearningSessionInactive, groupLearningSessionMCSN, groupLearningSessionCSN, groupLearningSessionLCB, groupLearningSessionLct) VALUES (NEW.groupLearningSessionUid, NEW.groupLearningSessionContentUid, NEW.groupLearningSessionLearnerGroupUid, NEW.groupLearningSessionInactive, NEW.groupLearningSessionMCSN, NEW.groupLearningSessionCSN, NEW.groupLearningSessionLCB, NEW.groupLearningSessionLct) " +
//             "/*psql ON CONFLICT (groupLearningSessionUid) DO UPDATE SET groupLearningSessionContentUid = EXCLUDED.groupLearningSessionContentUid, groupLearningSessionLearnerGroupUid = EXCLUDED.groupLearningSessionLearnerGroupUid, groupLearningSessionInactive = EXCLUDED.groupLearningSessionInactive, groupLearningSessionMCSN = EXCLUDED.groupLearningSessionMCSN, groupLearningSessionCSN = EXCLUDED.groupLearningSessionCSN, groupLearningSessionLCB = EXCLUDED.groupLearningSessionLCB, groupLearningSessionLct = EXCLUDED.groupLearningSessionLct*/"
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
  primaryKeys = arrayOf("glsPk", "glsDestination"),
  indices = arrayOf(Index(value = arrayOf("glsDestination", "glsProcessed", "glsPk")))
)
@Serializable
public class GroupLearningSessionReplicate {
  @ReplicationEntityForeignKey
  public var glsPk: Long = 0

  @ReplicationVersionId
  public var glsVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var glsDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var glsProcessed: Boolean = false
}
