package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = AgentEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
 Trigger(name = "agententity_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
         sqlStatements = [TRIGGER_UPSERT]
 )
 )
)
/**
 * @param agentUid The uid is the XXHash64 of the mbox, openId, or
 *        "agentAccountName@agentaHomePage" depending on which type
 *        of identifier is used.
 */
@Serializable
data class AgentEntity(
    @PrimaryKey
    var agentUid: Long = 0,

    var agentMbox: String? = null,

    var agentMbox_sha1sum: String? = null,

    var agentOpenid: String? = null,

    var agentAccountName: String? = null,

    var agentHomePage: String? = null,

    @ReplicateEtag
    @ReplicateLastModified
    var agentLct: Long = 0,
) {

    companion object {

        const val TABLE_ID = 68
    }
}
