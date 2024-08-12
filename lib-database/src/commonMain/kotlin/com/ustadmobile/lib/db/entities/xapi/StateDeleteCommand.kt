package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import kotlinx.serialization.Serializable

/**
 * Xapi Delete commands can require the deletion of all state ids for a particular context (agent +
 * activityId). If the device is offline, and other state ids for the context exist, then this is a
 * problem.
 *
 * A trigger will fire when StateDeleteCommand is inserted or updated to action the delete command,
 * e.g. when a delete all state ids for context occurs offline, then the delete command will be
 * sent upstream when a connection is next available (as usual), and when it reaches the server the
 * trigger will delete any applicable StateEntity(s) on the upstream server.
 *
 * See DeleteXapiStateUseCase for details.
 *
 * @param sdcActorUid the actorUid for the state
 * @param sdcHash - hash of other keys that are part of the identifier - activityId,
 *        registrationUuid (if included), stateId (if included)
 *
 */
@Entity(
    primaryKeys = arrayOf("sdcActorUid", "sdcHash")
)
@Serializable
@ReplicateEntity(
    tableId = StateDeleteCommand.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "statedeletecommand_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)

data class StateDeleteCommand(
    var sdcActorUid: Long = 0,
    var sdcHash: Long = 0,
    var sdcActivityUid: Long = 0,
    var sdcStateId: String? = null,
    @ReplicateEtag
    @ReplicateLastModified
    var sdcLastMod: Long = 0,
    var sdcRegistrationHi: Long? = null,
    var sdcRegistrationLo: Long? = null,
) {

    companion object {

        const val TABLE_ID = 121422

    }

}