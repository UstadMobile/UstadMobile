package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import kotlinx.serialization.Serializable

/**
 * Represents an xAPI session - where a particular user launches a particular activity. An entity
 * is created in the database when:
 *  a) xAPI content is being served over http
 *  b) The desktop version of the app is running an epub (which must submit xAPI statements over
 *     http without using the database in the browser).
 *
 * @param xseAuth - the expected authorization - a randomly generated password to use for basic auth.
 *        This is the password component ONLY. The client should send xseUid:xseAuth base64 encoded
 *        so that the server can then lookup the session and validate the auth.
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

    var xseAccountUsername: String? = null,

    var xseClazzUid: Long = 0,

    var xseCbUid: Long = 0,

    var xseContentEntryUid: Long = 0,

    var xseRootActivityId: String? = null,

    var xseStartTime: Long = 0,

    var xseExpireTime: Long = Long.MAX_VALUE,

    var xseAuth: String? = null,

) {
    companion object {

        const val TABLE_ID = 400122

    }
}