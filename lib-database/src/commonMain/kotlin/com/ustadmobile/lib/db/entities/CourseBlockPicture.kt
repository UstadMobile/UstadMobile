package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = CourseBlockPicture.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(
    arrayOf(
        Trigger(
            name = "courseblockpicture_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [ TRIGGER_UPSERT ]
        )
    )
)
data class CourseBlockPicture(
    /**
     * Id must be the same as CourseBlock cbUid
     */
    @PrimaryKey
    var cbpUid: Long = 0,

    @ReplicateLastModified
    @ReplicateEtag
    var cbpLct: Long = 0,

    var cbpPictureUri: String? = null,

    var cbpThumbnailUri: String? = null,

)  {

    companion object {

        const val TABLE_ID = 6677


    }

}
