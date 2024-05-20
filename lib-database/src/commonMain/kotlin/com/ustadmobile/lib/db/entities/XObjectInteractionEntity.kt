package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import kotlinx.serialization.Serializable

/**
 * Represents an interaction component as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#interaction-activities
 *
 * Used as a 1:many join with Xobject (xoieObjectUid is the foreign key)
 *
 * @param xoieObjectUid foreign key (XObjectEntity.xObjectUid)
 * @param xoieIdHash hash of "$xoieProp$xoieId" used to uniquely identify the Interaction component
 *        within a given Object.
 * @param xoieProp one of the PROP_ constants
 * @param xoieId the id of this choice as per the spec
 * @param xoieLastMod last modified time.
 */
@Entity(
    primaryKeys = arrayOf("xoieObjectUid", "xoieIdHash")
)
@Serializable
@ReplicateEntity(
    tableId =  XObjectInteractionEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "xobjectinteractionentity_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [ Trigger.Event.INSERT ],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)
data class XObjectInteractionEntity(
    var xoieObjectUid: Long = 0,
    var xoieIdHash: Long = 0,
    var xoieProp: Int = 0,
    var xoieId: String? = null,
    @ReplicateLastModified
    @ReplicateEtag
    var xoieLastMod: Long = 0,
) {

    companion object {

        const val PROP_CHOICES = 1

        const val PROP_SCALE = 2

        const val PROP_SOURCE = 3

        const val PROP_TARGET = 4

        const val PROP_STEPS = 5

        const val TABLE_ID = 6401

    }

}

