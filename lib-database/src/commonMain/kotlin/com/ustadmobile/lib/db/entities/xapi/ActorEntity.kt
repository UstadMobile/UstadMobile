package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = ActorEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "agententity_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [TRIGGER_UPSERT]
        )
    )
)
/**
 * Represents an xAPI Actor (Agent or Group). Various properties on a Statement are an actor that
 * can be a group or agent e.g. the actor themselves, instructor / team context, etc.
 *
 * Where the ActorEntity represents a group, then GroupMemberActorJoin is used to join the group
 * ActorEntity to a list of agent ActorEntities (members).
 *
 * @param actorUid For an agent or identified group, this is the XXHash64 of the mbox, openId, or
 *        "agentAccountName@agentaHomePage" depending on which type
 *        of identifier is used. For an anonymous group this uses the door primary key manager.
 *
 * @param actorEtag For an Agent this is the hash of the accountName (if non-null), otherwise,
 * 0. For a Group this is the hash of all members hash, where the member
 * list is sorted by the hash. This makes it easy to detect if an identified Group has been modified
 * (which means that all GroupMemberActorJoin last modified times will need updated).
 */
@Serializable
data class ActorEntity(
    @PrimaryKey(autoGenerate = true)
    var actorUid: Long = 0,

    var actorName: String? = null,

    var actorMbox: String? = null,

    var actorMbox_sha1sum: String? = null,

    var actorOpenid: String? = null,

    var actorAccountName: String? = null,

    var actorAccountHomePage: String? = null,

    @ReplicateEtag
    var actorEtag: Long = 0,

    @ReplicateLastModified(autoSet = false)
    var actorLct: Long = 0,

    var actorObjectType: Int = OBJECT_TYPE_AGENT,
) {

    companion object {

        const val OBJECT_TYPE_AGENT = 1

        const val OBJECT_TYPE_GROUP = 2

        const val TABLE_ID = 68
    }
}
