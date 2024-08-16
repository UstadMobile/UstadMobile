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

@Serializable
@ReplicateEntity(
    tableId = ActivityLangMapEntry.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(
    arrayOf(
        Trigger(
            name = "activitylangmapentry_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)
@Entity(
    primaryKeys = arrayOf("almeActivityUid", "almeHash")
)
/**
 * Entity used to store the string values for the langmap of Activity's name or display properties,
 * and the string values for the langmap associated with any of the interaction properties.
 *
 * @param almeActivityUid the activity uid that this lang map is related to
 * @param almeHash hash of "propertyname-langcode" where  propertyname is PROPNAME_NAME or
 * PROPNAME_DESCRIPTION as per PROP_NAME_constants and langcode is almeLangCode OR
 * an interaction property name (choices,scale,source,target,steps) - id - lang code e.g.
 * "choices-choiceid-en-US"
 * @param almeLangCode the lang code as per the xAPI language map eg en-US
 * @param almeValue the string value for the given language
 * @param almeAieHash where this entity represents a langmap for an interaction property, the hash
 * as per ActivityInteractionEntity.aieHash
 */
data class ActivityLangMapEntry(
    var almeActivityUid: Long = 0,
    var almeHash: Long = 0,
    var almeLangCode: String? = null,
    var almeValue: String? = null,
    var almeAieHash: Long = 0,

    @ReplicateLastModified
    @ReplicateEtag
    var almeLastMod: Long = 0,
) {
    companion object {

        const val TABLE_ID = 6442

        const val PROPNAME_NAME = "name"

        const val PROPNAME_DESCRIPTION = "description"

    }
}

