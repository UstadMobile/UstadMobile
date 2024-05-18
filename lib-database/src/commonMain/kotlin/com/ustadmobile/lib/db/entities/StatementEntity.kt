package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * @param statementIdHi the hi bits of the statement id (which is a UUID)
 * @param statementIdLo the lo bits of the statement id (which is a UUID)
 * @param fullStatement the JSON for the statement as per the 'exact' representation spec of xAPI
 *        as per statement immutability rules :
 *        https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#231-statement-immutability.
 *        The canonical JSON will be generated based on the entity fields.
 * @param resultDuration the duration of the result in ms (if provided), otherwise 0
 */
@Entity(
    primaryKeys = arrayOf("statementIdHi", "statementIdLo")
)
@Serializable
@ReplicateEntity(
    tableId = StatementEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
 Trigger(
     name = "statemententity_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [TRIGGER_UPSERT],
 )
))
data class StatementEntity(
    var statementIdHi: Long = 0,

    var statementIdLo: Long = 0,

    @ColumnInfo(index = true)
    var statementActorPersonUid: Long = 0,

    var statementVerbUid: Long = 0,

    var xObjectUid: Long = 0,

    var subStatementActorUid: Long = 0,

    var subStatementVerbUid: Long = 0,

    var subStatementObjectUid: Long = 0,

    var agentUid: Long = 0,

    var instructorUid: Long = 0,

    var authorityUid: Long = 0,

    var teamUid: Long = 0,

    var resultCompletion: Boolean = false,

    var resultSuccess: Byte = RESULT_UNSET,

    var resultScoreScaled: Float = 0f,

    var resultScoreRaw: Long = 0,

    var resultScoreMin: Long = 0,

    var resultScoreMax: Long = 0,

    var resultDuration: Long = 0,

    var resultResponse: String? = null,

    var timestamp: Long = 0,

    var stored: Long = 0,

    var contextRegistrationHi: Long = 0,

    var contextRegistrationLo: Long = 0,

    var contextPlatform: String? = null,

    var contextStatementRefIdHi: Long = 0,

    var contextStatementRefIdLo: Long = 0,

    var fullStatement: String? = null,

    @ReplicateLastModified
    @ReplicateEtag
    var statementLct: Long = 0,

    var extensionProgress: Int = 0,

    /**
     *  indicates whether or not the statement is about the root contentEntry or child entries
     *  This is used by queries (e.g. for reports) e.g. to see if a "completed" verb applies
     *  to the contententry itself, or only a subsection (child) of the content
     */
    var contentEntryRoot: Boolean = false,

    /**
     * Though technically the XObject is what really links to ContentEntry, the ContentEntryUid is
     * here to simplify queries used to check on student progress and avoid an extra join
     */
    var statementContentEntryUid: Long = 0,

    var statementLearnerGroupUid: Long = 0,

    var statementClazzUid: Long = 0,

    var statementCbUid: Long = 0,

    var statementDoorNode: Long = 0,
) {

    companion object {

        const val TABLE_ID = 60

        const val RESULT_UNSET = 0.toByte()

        const val RESULT_SUCCESS = 2.toByte()

        const val RESULT_FAILURE = 1.toByte()

        const val CONTENT_COMPLETE = 100

        const val CONTENT_INCOMPLETE = 101

        const val CONTENT_PASSED = 102

        const val CONTENT_FAILED = 103


    }
}
