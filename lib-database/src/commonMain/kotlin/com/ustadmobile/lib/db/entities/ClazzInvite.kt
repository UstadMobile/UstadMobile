package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzInvite.Companion.TABLE_ID
import kotlinx.serialization.Serializable


@Entity
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(
    arrayOf(
        Trigger(
            name = "clazzinvite_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [TRIGGER_UPSERT],
        )
    )
)


@Serializable
data class ClazzInvite(
    @PrimaryKey(autoGenerate = true)
    var ciUid: Long = 0,

    var ciPersonUid: Long = 0,

    var ciRoleId: Long = 0,

    var ciClazzUid: Long = 0,

    @ColumnInfo(defaultValue = "1")
    var inviteType: Int = 1,

    var inviteContact: String? = null,

    var inviteToken: String? = null,

    @ReplicateEtag
    @ReplicateLastModified
    var inviteLct: Long = 0
) {


    companion object {
        const val TABLE_ID = 521

        const val EMAIL = 1
        const val PHONE = 2
        const val INTERNAL_MESSAGE = 3


    }


}