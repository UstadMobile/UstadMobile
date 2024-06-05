package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import kotlinx.serialization.Serializable

/**
 * @param statementIdHi the hi bits of the statement id (which is a UUID)
 * @param statementIdLo the lo bits of the statement id (which is a UUID)
 * @param fullStatement the JSON for the statement as per the 'exact' representation spec of xAPI
 *        as per statement immutability rules :
 *        https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#231-statement-immutability.
 *        The canonical JSON will be generated based on the entity fields.
 * @param resultDuration the duration of the result in ms (if provided), otherwise 0
 * @param extensionProgress Captures the progress extension ( as per
 *        https://aicc.github.io/CMI-5_Spec_Current/samples/scenarios/13-progress_usage/ ) for use
 *        to show progress in the UI.
 * @param statementActorUid the ActorEntity.actorUid for the actor referenced by the actor property
 * @param statementObjectType the object type of the statement as a flag : Activity, Agent, Group,
 * StatementRef, or SubStatement
 * @param statementObjectUid1 where the object type is an Activity, Agent, or Group the uid of the
 * respective entity. When a StatementRef, the most significant uuid bits (hi). When a SubStatement,
 * the statementId will be set to the same as statementIdHi
 * @param statementObjectUid2 where the object type is an Activity, Agent, or Group, then 0. When a
 * StatementRef, the least significant uuid bits (lo). When a substatement, then statementIdLo + 1
 * @param isSubStatement if true, this is a substatement which cannot be independently retrieved.
 */
@Entity(
    primaryKeys = arrayOf("statementIdHi", "statementIdLo"),
    indices = arrayOf(
        Index(value = arrayOf("statementActorPersonUid"), name = "idx_stmt_actor_person")
    )
)
@Serializable
@ReplicateEntity(
    tableId = StatementEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
             name = "statemententity_remote_insert",
             order = Trigger.Order.INSTEAD_OF,
             on = Trigger.On.RECEIVEVIEW,
             events = [Trigger.Event.INSERT],
             conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
             sqlStatements = [TRIGGER_UPSERT],
        )
    )
)
data class StatementEntity(
    var statementIdHi: Long = 0,

    var statementIdLo: Long = 0,

    var statementActorPersonUid: Long = 0,

    var statementVerbUid: Long = 0,

    //As per the spec could be Activity, Agent, Group, StatementRef, or SubStatement
    var statementObjectType: Int = 0,

    var statementObjectUid1: Long = 0,

    var statementObjectUid2: Long = 0,

    var statementActorUid: Long = 0,

    var authorityUid: Long = 0,

    var teamUid: Long = 0,

    var resultCompletion: Boolean? = null,

    var resultSuccess: Boolean? = null,

    var resultScoreScaled: Float? = null,

    var resultScoreRaw: Float? = null,

    var resultScoreMin: Float? = null,

    var resultScoreMax: Float? = null,

    var resultDuration: Long? = null,

    var resultResponse: String? = null,

    var timestamp: Long = 0,

    var stored: Long = 0,

    var contextRegistrationHi: Long = 0,

    var contextRegistrationLo: Long = 0,

    var contextPlatform: String? = null,

    var contextStatementRefIdHi: Long = 0,

    var contextStatementRefIdLo: Long = 0,

    var contextInstructorUid: Long = 0,

    var fullStatement: String? = null,

    @ReplicateLastModified
    @ReplicateEtag
    var statementLct: Long = 0,

    var extensionProgress: Int? = null,

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

    var isSubStatement: Boolean = false,
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
