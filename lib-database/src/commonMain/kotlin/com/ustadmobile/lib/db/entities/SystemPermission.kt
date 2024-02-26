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

@Entity(
    indices = arrayOf(
        Index("spToPersonUid", name = "idx_systempermission_personuid")
    )
)
@ReplicateEntity(
    tableId = SystemPermission.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
    Trigger(
        name = "systempermission_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT],
    )
))
@Serializable
data class SystemPermission(
    @PrimaryKey(autoGenerate = true)
    var spUid: Long = 0,

    var spToPersonUid: Long = 0,

    var spToGroupUid: Long = 0,

    var spPermissionsFlag: Long = 0,

    @ReplicateEtag
    @ReplicateLastModified
    var spLastModified: Long = 0,

    var spIsDeleted: Boolean = false,
) {

    companion object {

        const val PERMISSION_ADD_COURSE: Long = 128L

        const val TABLE_ID = 10011
    }


}