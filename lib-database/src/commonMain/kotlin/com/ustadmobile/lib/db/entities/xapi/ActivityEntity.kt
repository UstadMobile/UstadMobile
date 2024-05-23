package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = ActivityEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
 Trigger(
     name = "activityentity_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [ TRIGGER_UPSERT ]
 )
))
/**
 * @param actCorrectResponsePatterns the JSON of the correct responses pattern (array of strings)
 */
data class ActivityEntity(

    @PrimaryKey
    var actUid: Long = 0,

    var actIdIri: String? = null,

    var actType: String? = null,

    var actMoreInfo: String? = null,

    var actInteractionType: String? = null,

    var actCorrectResponsePatterns: String? = null,

    @ReplicateLastModified
    @ReplicateEtag
    var actLct: Long = 0,

) {

    companion object {

        const val TABLE_ID = 64
    }
}

