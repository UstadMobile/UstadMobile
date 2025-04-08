package com.ustadmobile.lib.db.entities.respect

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import com.ustadmobile.lib.db.entities.respect.RespectAssignment.Companion.TABLE_ID
import kotlinx.serialization.Serializable

/**
 * @param razUid auto generated primary key
 * @param razRlUid foreign key join to respect lesson uid
 * @param razTitle assignment title
 * @param razDescription HTML text/description
 * @param razDeadline assignment deadline (ms since epoch)
 * @param razToClazzUid foreign key assignment to clazzUid
 */
@Entity
@Serializable

@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(
    arrayOf(
        Trigger(
            name = "respectassignment_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [TRIGGER_UPSERT],
        )
    )
)
data class RespectAssignment(
    @PrimaryKey(autoGenerate = true)
    var razUid: Long = 0,

    var razToClazzUid: Long = 0,

    var razTitle: String = "",

    var razDescription: String = "",

    var razRlUid: Long = 0,

    var razDeadline: Long = 0,

    @ReplicateEtag
    @ReplicateLastModified
    var razLastMod: Long = 0,
) {

    companion object {

        const val TABLE_ID = 4206

    }

}
