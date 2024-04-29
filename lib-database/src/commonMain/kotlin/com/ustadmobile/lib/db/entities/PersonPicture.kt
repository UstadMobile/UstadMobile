package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = PersonPicture.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
 Trigger(
     name = "personpicture_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [ TRIGGER_UPSERT ]
 )
))
data class PersonPicture(
    /**
     * The personPictureUid should match the personUid
     */
    @PrimaryKey(autoGenerate = true)
    var personPictureUid: Long = 0,

    @ReplicateLastModified
    @ReplicateEtag
    var personPictureLct: Long = 0,

    var personPictureUri: String? = null,

    var personPictureThumbnailUri: String? = null,

    var fileSize: Int = 0,

    var personPictureActive: Boolean = true,

) {

    companion object {

        const val TABLE_ID = 50
    }


}
