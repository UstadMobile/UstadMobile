package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity(indices = [
    // index used to cache the best assignments from the statements
    Index(value = ["statementContentEntryUid","statementPersonUid","contentEntryRoot",
                    "timestamp","statementLocalChangeSeqNum"])
])
@Serializable
//This must be replicated before entities that are joined to it e.g. ContextXObjectStatementJoin
@ReplicateEntity(tableId = StatementEntity.TABLE_ID, tracker = StatementEntityReplicate::class,
    ReplicateEntity.HIGHEST_PRIORITY + 1)
@Triggers(arrayOf(
 Trigger(
     name = "statemententity_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO StatementEntity(statementUid, statementId, statementPersonUid, statementVerbUid, xObjectUid, subStatementActorUid, substatementVerbUid, subStatementObjectUid, agentUid, instructorUid, authorityUid, teamUid, resultCompletion, resultSuccess, resultScoreScaled, resultScoreRaw, resultScoreMin, resultScoreMax, resultDuration, resultResponse, timestamp, stored, contextRegistration, contextPlatform, contextStatementId, fullStatement, statementMasterChangeSeqNum, statementLocalChangeSeqNum, statementLastChangedBy, statementLct, extensionProgress, contentEntryRoot, statementContentEntryUid, statementLearnerGroupUid, statementClazzUid) 
         VALUES (NEW.statementUid, NEW.statementId, NEW.statementPersonUid, NEW.statementVerbUid, NEW.xObjectUid, NEW.subStatementActorUid, NEW.substatementVerbUid, NEW.subStatementObjectUid, NEW.agentUid, NEW.instructorUid, NEW.authorityUid, NEW.teamUid, NEW.resultCompletion, NEW.resultSuccess, NEW.resultScoreScaled, NEW.resultScoreRaw, NEW.resultScoreMin, NEW.resultScoreMax, NEW.resultDuration, NEW.resultResponse, NEW.timestamp, NEW.stored, NEW.contextRegistration, NEW.contextPlatform, NEW.contextStatementId, NEW.fullStatement, NEW.statementMasterChangeSeqNum, NEW.statementLocalChangeSeqNum, NEW.statementLastChangedBy, NEW.statementLct, NEW.extensionProgress, NEW.contentEntryRoot, NEW.statementContentEntryUid, NEW.statementLearnerGroupUid, NEW.statementClazzUid) 
         /*psql ON CONFLICT (statementUid) DO UPDATE 
         SET statementId = EXCLUDED.statementId, statementPersonUid = EXCLUDED.statementPersonUid, statementVerbUid = EXCLUDED.statementVerbUid, xObjectUid = EXCLUDED.xObjectUid, subStatementActorUid = EXCLUDED.subStatementActorUid, substatementVerbUid = EXCLUDED.substatementVerbUid, subStatementObjectUid = EXCLUDED.subStatementObjectUid, agentUid = EXCLUDED.agentUid, instructorUid = EXCLUDED.instructorUid, authorityUid = EXCLUDED.authorityUid, teamUid = EXCLUDED.teamUid, resultCompletion = EXCLUDED.resultCompletion, resultSuccess = EXCLUDED.resultSuccess, resultScoreScaled = EXCLUDED.resultScoreScaled, resultScoreRaw = EXCLUDED.resultScoreRaw, resultScoreMin = EXCLUDED.resultScoreMin, resultScoreMax = EXCLUDED.resultScoreMax, resultDuration = EXCLUDED.resultDuration, resultResponse = EXCLUDED.resultResponse, timestamp = EXCLUDED.timestamp, stored = EXCLUDED.stored, contextRegistration = EXCLUDED.contextRegistration, contextPlatform = EXCLUDED.contextPlatform, contextStatementId = EXCLUDED.contextStatementId, fullStatement = EXCLUDED.fullStatement, statementMasterChangeSeqNum = EXCLUDED.statementMasterChangeSeqNum, statementLocalChangeSeqNum = EXCLUDED.statementLocalChangeSeqNum, statementLastChangedBy = EXCLUDED.statementLastChangedBy, statementLct = EXCLUDED.statementLct, extensionProgress = EXCLUDED.extensionProgress, contentEntryRoot = EXCLUDED.contentEntryRoot, statementContentEntryUid = EXCLUDED.statementContentEntryUid, statementLearnerGroupUid = EXCLUDED.statementLearnerGroupUid, statementClazzUid = EXCLUDED.statementClazzUid
         */"""
     ]
 )
))
open class StatementEntity {

    @PrimaryKey(autoGenerate = true)
    var statementUid: Long = 0

    var statementId: String? = null

    @ColumnInfo(index = true)
    var statementPersonUid: Long = 0

    var statementVerbUid: Long = 0

    var xObjectUid: Long = 0

    var subStatementActorUid: Long = 0

    var substatementVerbUid: Long = 0

    var subStatementObjectUid: Long = 0

    var agentUid: Long = 0

    var instructorUid: Long = 0

    var authorityUid: Long = 0

    var teamUid: Long = 0

    var resultCompletion: Boolean = false

    var resultSuccess: Byte = RESULT_UNSET

    var resultScoreScaled: Float = 0f

    var resultScoreRaw: Long = 0

    var resultScoreMin: Long = 0

    var resultScoreMax: Long = 0

    var resultDuration: Long = 0

    var resultResponse: String? = null

    var timestamp: Long = 0

    var stored: Long = 0

    var contextRegistration: String? = null

    var contextPlatform: String? = null

    var contextStatementId: String? = null

    var fullStatement: String? = null

    @MasterChangeSeqNum
    var statementMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var statementLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var statementLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var statementLct: Long = 0

    var extensionProgress: Int = 0

    /**
     *  indicates whether or not the statement is about the root contentEntry or child entries
     *  This is used by queries (e.g. for reports) e.g. to see if a "completed" verb applies
     *  to the contententry itself, or only a subsection (child) of the content
     */
    var contentEntryRoot: Boolean = false

    /**
     * Though technically the XObject is what really links to ContentEntry, the ContentEntryUid is
     * here to simplify queries used to check on student progress and avoid an extra join
     */
    var statementContentEntryUid: Long = 0


    var statementLearnerGroupUid: Long = 0

    var statementClazzUid: Long = 0

    companion object {

        const val TABLE_ID = 60

        const val RESULT_UNSET = 0.toByte()

        const val RESULT_SUCCESS = 2.toByte()

        const val RESULT_FAILURE = 1.toByte()

        const val CONTENT_COMPLETE = 100

        const val CONTENT_INCOMPLETE = 101

        const val CONTENT_PASSED = 102

        const val CONTENT_FAILED = 103


        const val FROM_STATEMENT_TO_SCOPEDGRANT_JOIN_ON_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
             OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                AND ScopedGrant.sgEntityUid = StatementEntity.statementPersonUid)
             OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                AND ScopedGrant.sgEntityUid = StatementEntity.statementClazzUid)
             OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                AND ScopedGrant.sgEntityUid = (
                    SELECT clazzSchoolUid
                      FROM Clazz
                     WHERE clazzUid = StatementEntity.statementClazzUid))
             )
        """


        const val FROM_SCOPEDGRANT_TO_STATEMENT_JOIN_ON_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
             OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                AND ScopedGrant.sgEntityUid = StatementEntity.statementPersonUid)
             OR (ScopedGrant.sgTableId = ${Clazz.TABLE_ID}
                AND ScopedGrant.sgEntityUid = StatementEntity.statementClazzUid)
             OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                AND ScopedGrant.sgEntityUid = (
                    SELECT clazzSchoolUid
                      FROM Clazz 
                     WHERE clazzUid = StatementEntity.statementClazzUid))
            )         
        """


    }
}
