package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId =  XObjectEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
 Trigger(
     name = "xobjectentity_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [ TRIGGER_UPSERT ]
 )
))
/**
 * @param correctResponsePattern the JSON of the correct responses pattern (array of strings)
 */
class XObjectEntity(

    @PrimaryKey
    var xObjectUid: Long = 0,

    var objectType: String? = null,

    var objectId: String? = null,

    var definitionType: String? = null,

    var interactionType: String? = null,

    var definitionMoreInfo: String? = null,

    var correctResponsePattern: String? = null,

    @ReplicateLastModified
    @ReplicateEtag
    var xObjectLct: Long = 0,

) {

    companion object {

        const val TABLE_ID = 64
    }
}

