package com.ustadmobile.lib.db.entities.xapi

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import kotlinx.serialization.Serializable

/**
 * Represents an xAPI session. U
 *
 * @param xseAuth - the expected authorization - a randomly generated password to use for basic auth.
 *        This is the password component ONLY. The client should send xseUid:xseAuth base64 encoded
 *        so that the server can then lookup the session and validate the auth.
 * @param xseCompleted - this will be set to true by the StatementResource when a statement indicates
 *        that the content has been completed (e.g. normally when
 *        StatementEntity.completionOrProgress = true and StatementEntity.resultCompletion = true,
 *        this may vary depending on completion criteria set by the teacher/admin).
 * @param knownActorUidToPersonUids A map of known actor uids to personuids for the relevant session,
 *        used by toEntities functions in the xapi model to ensure that any created ActorEntity will
 *        be have the actorPersonUid set correctly.
 */
@Entity
@Serializable
@ReplicateEntity(
    tableId = XapiSessionEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "xapisessionentity_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)
data class XapiSessionEntity(
    @PrimaryKey(autoGenerate = true)
    var xseUid: Long = 0,

    @ReplicateEtag
    @ReplicateLastModified
    var xseLastMod: Long = 0,

    var xseRegistrationHi: Long = 0,

    var xseRegistrationLo: Long = 0,

    var xseUsUid: Long = 0,

    var xseAccountPersonUid: Long = 0,

    var xseActorUid: Long = 0,

    var xseAccountUsername: String = "",

    var xseClazzUid: Long = 0,

    var xseCbUid: Long = 0,

    var xseContentEntryUid: Long = 0,

    @ColumnInfo(defaultValue = "0")
    var xseContentEntryVersionUid: Long = 0,

    var xseRootActivityId: String = "",

    var xseRootActivityUid: Long = 0,

    var xseStartTime: Long = systemTimeInMillis(),

    var xseExpireTime: Long = Long.MAX_VALUE,

    var xseAuth: String? = null,

    @ColumnInfo(defaultValue = "0")
    var xseCompleted: Boolean = false,

    var knownActorUidToPersonUids: String = "",

) {
    companion object {

        const val TABLE_ID = 400122

    }
}