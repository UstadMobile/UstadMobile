package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT

@Entity(primaryKeys = arrayOf("aeeActivityUid", "aeeKeyHash"))
@ReplicateEntity(
    tableId = ActivityExtensionEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "activityextensionentity_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [ Trigger.Event.INSERT ],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)
data class ActivityExtensionEntity(
    var aeeActivityUid: Long = 0,
    var aeeKeyHash: Long = 0,
    var aeeKey: String? = null,
    var aeeJson: String? = null,
    @ReplicateLastModified
    @ReplicateEtag
    var aeeLastMod: Long = 0,
    var aeeIsDeleted: Boolean = false,
) {
    companion object {
        const val TABLE_ID = 6405
    }
}
