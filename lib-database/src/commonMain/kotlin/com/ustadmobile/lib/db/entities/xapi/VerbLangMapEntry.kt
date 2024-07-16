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
    primaryKeys = arrayOf("vlmeVerbUid", "vlmeLangHash")
)
@Serializable
@ReplicateEntity(
    tableId = VerbLangMapEntry.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(
    arrayOf(
        Trigger(
            name = "verblangmapentry_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)

/**
 * Use in a one to many join with VerbEntity. Verb display can be updated. When statements are
 * queried using canonical mode, we need to be able to return the the latest display langmap.
 *
 * @param vlmeVerbUid the foreign key e.g. VerbEntity.verbUid (xxhash of the Verb's id url)
 * @param vlmeLangHash the xxhash of the language code as per the lang map e.g. en-US
 * @param vlmeEntryString the actual string e.g. as will be displayed to the user
 * @param vlmeLangCode the lang code as per the Language Map
 * @param vlmeLastModified the last time this entry was modified
 */
data class VerbLangMapEntry(
    var vlmeVerbUid: Long = 0L,
    var vlmeLangHash: Long = 0L,
    var vlmeLangCode: String? = null,
    var vlmeEntryString: String? = null,
    @ReplicateLastModified
    @ReplicateEtag
    var vlmeLastModified: Long = 0,
) {
    companion object {

        const val TABLE_ID = 620

    }
}
