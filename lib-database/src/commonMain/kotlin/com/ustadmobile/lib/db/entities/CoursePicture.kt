package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@EntityWithAttachment
@ReplicateEntity(
    tableId = CoursePicture.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
 Trigger(
     name = "coursepicture_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [
         TRIGGER_UPSERT_WHERE_NEWER
     ]
 )
))
data class CoursePicture(
    @PrimaryKey(autoGenerate = true)
    var coursePictureUid: Long = 0,

    @ReplicateLastModified
    @ReplicateEtag
    var coursePictureLct: Long = 0,

    var coursePictureUri: String? = null,

    var coursePictureThumbnailUri: String? =null,

    var coursePictureActive: Boolean = true
) {

    companion object {

        const val TABLE_ID = 125
    }

}
