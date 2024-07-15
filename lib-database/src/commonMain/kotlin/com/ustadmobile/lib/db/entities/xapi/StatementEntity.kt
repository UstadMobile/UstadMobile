package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import androidx.room.Index
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import kotlinx.serialization.Serializable

/**
 * The primary representation of an Xapi Statement in the database. Note that the full original
 * json is stored on StatementEntityJson (such that results data can be retrieved without downloading
 * the full json string).
 *
 * @param statementIdHi the hi bits of the statement id (which is a UUID)
 * @param statementIdLo the lo bits of the statement id (which is a UUID)
 * @param statementActorPersonUid where the actor is a single known person, the personUid. This is
 *        will be set for self-paced content such as videos / xAPI / H5P packages completed by an
 *        individual, but might not be set otherwise. It will not be set when the actor is a group
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
 * @param completionOrProgress Indicates whether or not the statement is completion or progress
 * (excludes xAPI statements that are progress or completion of child activities for statements
 * received over API) - e.g. the statement could be relevant to showing the progress of the learner.

 * This is used as an index field so that the database can quickly filter out other types of
 * statements (e.g. statements that are not for the top level activity, dont have a score,
 * completion status, etc). Given that Statements are click stream level, there are a lot
 * of them, so this index is important to help speed up queries on table that will get big.
 *
 * This is true if the statement has a result with a non null value for result score scaled
 */
@Entity(
    primaryKeys = arrayOf("statementIdHi", "statementIdLo"),
    indices = arrayOf(
        Index("statementActorPersonUid", name = "idx_stmt_actor_person"),
        Index("statementClazzUid", "statementActorPersonUid", name = "idx_statement_clazz_person"),

        //For gradebook report: searches first by courseblock, then actor
        Index("statementCbUid", "statementActorUid", name = "idx_statement_cbuid_actor")
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

    var authorityActorUid: Long = 0,

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

    var contextInstructorActorUid: Long = 0,

    @ReplicateLastModified
    @ReplicateEtag
    var statementLct: Long = 0,

    var extensionProgress: Int? = null,

    var completionOrProgress: Boolean = false,

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
