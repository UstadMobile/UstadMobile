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
 * Represents an interaction component as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#interaction-activities
 *
 * Used as a 1:many join with Xobject (xoieObjectUid is the foreign key)
 *
 * @param aieActivityUid activity uid (foreign key)
 * @param aieHash hash of "$aieProp$aieId" used to uniquely identify the Interaction component
 *        within a given Object.
 * @param aieProp one of the PROP_ constants
 * @param aieId the id of this choice as per the spec
 * @param aieLastMod last modified time.
 */
@Entity(
    primaryKeys = arrayOf("aieActivityUid", "aieHash")
)
@Serializable
@ReplicateEntity(
    tableId = ActivityInteractionEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "activityinteractionentity_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [ Trigger.Event.INSERT ],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)
data class ActivityInteractionEntity(
    var aieActivityUid: Long = 0,
    var aieHash: Long = 0,
    var aieProp: Int = 0,
    var aieId: String? = null,
    @ReplicateLastModified(autoSet = false)
    @ReplicateEtag
    var aieLastMod: Long = 0,
    var aieIsDeleted: Boolean = false,
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

