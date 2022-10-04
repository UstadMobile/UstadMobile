// @Triggers(arrayOf(
//     Trigger(
//         name = "statemententity_remote_insert",
//         order = Trigger.Order.INSTEAD_OF,
//         on = Trigger.On.RECEIVEVIEW,
//         events = [Trigger.Event.INSERT],
//         sqlStatements = [
//             """REPLACE INTO StatementEntity(statementUid, statementId, statementPersonUid, statementVerbUid, xObjectUid, subStatementActorUid, substatementVerbUid, subStatementObjectUid, agentUid, instructorUid, authorityUid, teamUid, resultCompletion, resultSuccess, resultScoreScaled, resultScoreRaw, resultScoreMin, resultScoreMax, resultDuration, resultResponse, timestamp, stored, contextRegistration, contextPlatform, contextStatementId, fullStatement, statementMasterChangeSeqNum, statementLocalChangeSeqNum, statementLastChangedBy, statementLct, extensionProgress, contentEntryRoot, statementContentEntryUid, statementLearnerGroupUid, statementClazzUid) 
//             VALUES (NEW.statementUid, NEW.statementId, NEW.statementPersonUid, NEW.statementVerbUid, NEW.xObjectUid, NEW.subStatementActorUid, NEW.substatementVerbUid, NEW.subStatementObjectUid, NEW.agentUid, NEW.instructorUid, NEW.authorityUid, NEW.teamUid, NEW.resultCompletion, NEW.resultSuccess, NEW.resultScoreScaled, NEW.resultScoreRaw, NEW.resultScoreMin, NEW.resultScoreMax, NEW.resultDuration, NEW.resultResponse, NEW.timestamp, NEW.stored, NEW.contextRegistration, NEW.contextPlatform, NEW.contextStatementId, NEW.fullStatement, NEW.statementMasterChangeSeqNum, NEW.statementLocalChangeSeqNum, NEW.statementLastChangedBy, NEW.statementLct, NEW.extensionProgress, NEW.contentEntryRoot, NEW.statementContentEntryUid, NEW.statementLearnerGroupUid, NEW.statementClazzUid) 
//             /*psql ON CONFLICT (statementUid) DO UPDATE 
//             SET statementId = EXCLUDED.statementId, statementPersonUid = EXCLUDED.statementPersonUid, statementVerbUid = EXCLUDED.statementVerbUid, xObjectUid = EXCLUDED.xObjectUid, subStatementActorUid = EXCLUDED.subStatementActorUid, substatementVerbUid = EXCLUDED.substatementVerbUid, subStatementObjectUid = EXCLUDED.subStatementObjectUid, agentUid = EXCLUDED.agentUid, instructorUid = EXCLUDED.instructorUid, authorityUid = EXCLUDED.authorityUid, teamUid = EXCLUDED.teamUid, resultCompletion = EXCLUDED.resultCompletion, resultSuccess = EXCLUDED.resultSuccess, resultScoreScaled = EXCLUDED.resultScoreScaled, resultScoreRaw = EXCLUDED.resultScoreRaw, resultScoreMin = EXCLUDED.resultScoreMin, resultScoreMax = EXCLUDED.resultScoreMax, resultDuration = EXCLUDED.resultDuration, resultResponse = EXCLUDED.resultResponse, timestamp = EXCLUDED.timestamp, stored = EXCLUDED.stored, contextRegistration = EXCLUDED.contextRegistration, contextPlatform = EXCLUDED.contextPlatform, contextStatementId = EXCLUDED.contextStatementId, fullStatement = EXCLUDED.fullStatement, statementMasterChangeSeqNum = EXCLUDED.statementMasterChangeSeqNum, statementLocalChangeSeqNum = EXCLUDED.statementLocalChangeSeqNum, statementLastChangedBy = EXCLUDED.statementLastChangedBy, statementLct = EXCLUDED.statementLct, extensionProgress = EXCLUDED.extensionProgress, contentEntryRoot = EXCLUDED.contentEntryRoot, statementContentEntryUid = EXCLUDED.statementContentEntryUid, statementLearnerGroupUid = EXCLUDED.statementLearnerGroupUid, statementClazzUid = EXCLUDED.statementClazzUid
//             */"""
//         ]
//     )
// ))                @Query("""
//     REPLACE INTO StatementEntityReplicate(sePk, seVersionId, seDestination)
//      SELECT StatementEntity.statementUid AS seUid,
//             StatementEntity.statementLct AS seVersionId,
//             :newNodeId AS seDestination
//        FROM StatementEntity
//       WHERE StatementEntity.statementLct != COALESCE(
//             (SELECT seVersionId
//                FROM StatementEntityReplicate
//               WHERE sePk = StatementEntity.statementUid
//                 AND seDestination = :newNodeId), 0) 
//      /*psql ON CONFLICT(sePk, seDestination) DO UPDATE
//             SET sePending = true
//      */       
// """)
// @ReplicationRunOnNewNode
// @ReplicationCheckPendingNotificationsFor([StatementEntity::class])
// abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)
// @Query("""
// REPLACE INTO StatementEntityReplicate(sePk, seVersionId, seDestination)
//  SELECT StatementEntity.statementUid AS seUid,
//         StatementEntity.statementLct AS seVersionId,
//         UserSession.usClientNodeId AS seDestination
//    FROM ChangeLog
//         JOIN StatementEntity
//             ON ChangeLog.chTableId = 60
//                AND ChangeLog.chEntityPk = StatementEntity.statementUid
//         JOIN UserSession
//   WHERE UserSession.usClientNodeId != (
//         SELECT nodeClientId 
//           FROM SyncNode
//          LIMIT 1)
//     AND StatementEntity.statementLct != COALESCE(
//         (SELECT seVersionId
//            FROM StatementEntityReplicate
//           WHERE sePk = StatementEntity.statementUid
//             AND seDestination = UserSession.usClientNodeId), 0)
// /*psql ON CONFLICT(sePk, seDestination) DO UPDATE
//     SET sePending = true
//  */               
// """)
// @ReplicationRunOnChange([StatementEntity::class])
// @ReplicationCheckPendingNotificationsFor([StatementEntity::class])
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
  primaryKeys = arrayOf("sePk", "seDestination"),
  indices = arrayOf(Index(value = arrayOf("sePk", "seDestination", "seVersionId")),
  Index(value = arrayOf("seDestination", "sePending")))

)
@Serializable
public class StatementEntityReplicate {
  @ReplicationEntityForeignKey
  public var sePk: Long = 0

  @ColumnInfo(defaultValue = "0")
  @ReplicationVersionId
  public var seVersionId: Long = 0

  @ReplicationDestinationNodeId
  public var seDestination: Long = 0

  @ColumnInfo(defaultValue = "1")
  @ReplicationPending
  public var sePending: Boolean = true
}
