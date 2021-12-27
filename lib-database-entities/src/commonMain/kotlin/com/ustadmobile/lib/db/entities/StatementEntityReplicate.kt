// @Triggers(arrayOf(
//     Trigger(name = "statemententity_remote_insert",
//             order = Trigger.Order.INSTEAD_OF,
//             on = Trigger.On.RECEIVEVIEW,
//             events = [Trigger.Event.INSERT],
//             sqlStatements = [
//             "REPLACE INTO StatementEntity(statementUid, statementId, statementPersonUid, statementVerbUid, xObjectUid, subStatementActorUid, substatementVerbUid, subStatementObjectUid, agentUid, instructorUid, authorityUid, teamUid, resultCompletion, resultSuccess, resultScoreScaled, resultScoreRaw, resultScoreMin, resultScoreMax, resultDuration, resultResponse, timestamp, stored, contextRegistration, contextPlatform, contextStatementId, fullStatement, statementMasterChangeSeqNum, statementLocalChangeSeqNum, statementLastChangedBy, statementLct, extensionProgress, contentEntryRoot, statementContentEntryUid, statementLearnerGroupUid, statementClazzUid) VALUES (NEW.statementUid, NEW.statementId, NEW.statementPersonUid, NEW.statementVerbUid, NEW.xObjectUid, NEW.subStatementActorUid, NEW.substatementVerbUid, NEW.subStatementObjectUid, NEW.agentUid, NEW.instructorUid, NEW.authorityUid, NEW.teamUid, NEW.resultCompletion, NEW.resultSuccess, NEW.resultScoreScaled, NEW.resultScoreRaw, NEW.resultScoreMin, NEW.resultScoreMax, NEW.resultDuration, NEW.resultResponse, NEW.timestamp, NEW.stored, NEW.contextRegistration, NEW.contextPlatform, NEW.contextStatementId, NEW.fullStatement, NEW.statementMasterChangeSeqNum, NEW.statementLocalChangeSeqNum, NEW.statementLastChangedBy, NEW.statementLct, NEW.extensionProgress, NEW.contentEntryRoot, NEW.statementContentEntryUid, NEW.statementLearnerGroupUid, NEW.statementClazzUid) " +
//             "/*psql ON CONFLICT (statementUid) DO UPDATE SET statementId = EXCLUDED.statementId, statementPersonUid = EXCLUDED.statementPersonUid, statementVerbUid = EXCLUDED.statementVerbUid, xObjectUid = EXCLUDED.xObjectUid, subStatementActorUid = EXCLUDED.subStatementActorUid, substatementVerbUid = EXCLUDED.substatementVerbUid, subStatementObjectUid = EXCLUDED.subStatementObjectUid, agentUid = EXCLUDED.agentUid, instructorUid = EXCLUDED.instructorUid, authorityUid = EXCLUDED.authorityUid, teamUid = EXCLUDED.teamUid, resultCompletion = EXCLUDED.resultCompletion, resultSuccess = EXCLUDED.resultSuccess, resultScoreScaled = EXCLUDED.resultScoreScaled, resultScoreRaw = EXCLUDED.resultScoreRaw, resultScoreMin = EXCLUDED.resultScoreMin, resultScoreMax = EXCLUDED.resultScoreMax, resultDuration = EXCLUDED.resultDuration, resultResponse = EXCLUDED.resultResponse, timestamp = EXCLUDED.timestamp, stored = EXCLUDED.stored, contextRegistration = EXCLUDED.contextRegistration, contextPlatform = EXCLUDED.contextPlatform, contextStatementId = EXCLUDED.contextStatementId, fullStatement = EXCLUDED.fullStatement, statementMasterChangeSeqNum = EXCLUDED.statementMasterChangeSeqNum, statementLocalChangeSeqNum = EXCLUDED.statementLocalChangeSeqNum, statementLastChangedBy = EXCLUDED.statementLastChangedBy, statementLct = EXCLUDED.statementLct, extensionProgress = EXCLUDED.extensionProgress, contentEntryRoot = EXCLUDED.contentEntryRoot, statementContentEntryUid = EXCLUDED.statementContentEntryUid, statementLearnerGroupUid = EXCLUDED.statementLearnerGroupUid, statementClazzUid = EXCLUDED.statementClazzUid*/"
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
  primaryKeys = arrayOf("sePk", "seDestination"),
  indices = arrayOf(Index(value = arrayOf("seDestination", "seProcessed", "sePk")))
)
@Serializable
public class StatementEntityReplicate {
  @ReplicationEntityForeignKey
  public var sePk: Long = 0

  @ReplicationVersionId
  public var seVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var seDestination: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationTrackerProcessed
  public var seProcessed: Boolean = false
}
