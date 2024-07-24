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

@Entity(
    primaryKeys = arrayOf("seActorUid", "seHash", "seKeyHash")
)
@Serializable
@ReplicateEntity(
    tableId = StateEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "stateentity_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)

/**
 * Used to store the xAPI State for State Resource as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#23-state-resource
 *
 * xAPI state json documents are merged according to top level properties, therefor there is one
 * StateEntity for each top level property (the entities that make up a single state document will
 * have the same seAgentUid and the same seHash, but a different seKey and seKeyHash).
 *
 * @param seActorUid Uid of the related actor entity (this MUST be an agent as per the spec Xapi
 *        Spec - Communication section 2.3)
 * @param seHash - hash of other keys that are part of the identifier - activityId, registrationUuid,
 *        stateId
 * @param seKey - top level property name
 * @param seKeyHash xxhash64 of seKey (used as part of the composite primary key)
 * @param seStateId the stateId as per the xAPI spec
 *
 */
data class StateEntity(
    var seActorUid: Long = 0,

    var seHash: Long  = 0,

    var seKey: String? = null,

    var seKeyHash: Long = 0,

    var seActivityUid: Long  = 0,

    var seStateId: String? = null,

    @ReplicateEtag
    @ReplicateLastModified
    var seLastMod: Long = 0,

    //Reserved for future use
    var seTimeStored: Long = 0,

    var seContent: String? = null,

    var seDeleted: Boolean = false,

    var seRegistrationHi: Long? = null,

    var seRegistrationLo: Long? = null,

) {

    companion object {

        const val TABLE_ID = 3289

    }
}