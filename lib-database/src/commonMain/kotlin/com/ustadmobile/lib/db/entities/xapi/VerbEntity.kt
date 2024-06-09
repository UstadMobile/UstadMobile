package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import com.ustadmobile.lib.db.entities.xapi.VerbEntity.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
 Trigger(
     name = "verbentity_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [ TRIGGER_UPSERT ]
 )
))
/**
 * Verb as per the xAPI spec. Verb only has two properties ( id and display ) as per the spec:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#243-verb
 *
 * Joins with VerbXLangMapEntry to handle the display langmap
 *
 * @param verbUid The XXHash64 of verbUrlId
 */
data class VerbEntity(
    @PrimaryKey
    var verbUid: Long = 0,

    var verbUrlId: String? = null,

    var verbDeleted: Boolean = false,

    @ReplicateLastModified
    @ReplicateEtag
    var verbLct: Long = 0,
) {


    companion object {

        const val TABLE_ID = 62

        const val VERB_COMPLETED_URL = "http://adlnet.gov/expapi/verbs/completed"

        const val VERB_COMPLETED_UID = 10001L

        const val VERB_PASSED_URL = "http://adlnet.gov/expapi/verbs/passed"

        const val VERB_PASSED_UID = 10002L

        const val VERB_FAILED_URL = "http://adlnet.gov/expapi/verbs/failed"

        const val VERB_FAILED_UID = 10003L

    }

}
