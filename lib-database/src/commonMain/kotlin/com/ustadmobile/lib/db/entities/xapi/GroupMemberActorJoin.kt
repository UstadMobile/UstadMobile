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
 * See XapiGroup.toGroupEntities for further details on the mapping between the Xapi Spec and the
 * database entities.
 */
@Entity(
    primaryKeys = arrayOf("gmajGroupActorUid", "gmajMemberActorUid")
)
@ReplicateEntity(
    tableId = GroupMemberActorJoin.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "groupmemberactorjoin_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)
@Serializable
data class GroupMemberActorJoin(
    var gmajGroupActorUid: Long = 0,
    var gmajMemberActorUid: Long = 0,

    //Not automatically set so the api can ensure this is an exact match with the Group ActorEntity
    @ReplicateLastModified(autoSet = false)
    @ReplicateEtag
    var gmajLastMod: Long = 0,
) {
    companion object {

        const val TABLE_ID = 4232

    }
}
