package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import kotlinx.serialization.Serializable

/**
 * Represents an item that a user has selected for offline access on a particular device (e.g. node).
 *
 * This can be used in queries/triggers to download blobs (e.g. profile pictures, course pictures, etc)
 * as required. It can also be used with ContentEntryVersion to trigger the download of a
 * ContentEntryVersion when the OfflineItem is created and when a new ContentEntryVersion is
 * available.
 *
 * This can also be used on the server side to select data to push to given clients.
 */
@Entity(
    indices = arrayOf(
        Index("oiNodeId", "oiContentEntryUid", name = "offline_item_node_content_entry")
    )
)
@ReplicateEntity(
    tableId = OfflineItem.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "offline_item_remote_ins",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [TRIGGER_UPSERT]
        )
    )
)
@Serializable
data class OfflineItem(
    @PrimaryKey(autoGenerate = true)
    var oiUid: Long = 0,
    var oiNodeId: Long = 0,
    var oiClazzUid: Long = 0,
    var oiCourseBlockUid: Long = 0,
    var oiContentEntryUid: Long = 0,
    var oiActive: Boolean = true,
    @ReplicateEtag
    @ReplicateLastModified
    var oiLct: Long = 0,
) {
    companion object {

        const val TABLE_ID = 971

    }
}
