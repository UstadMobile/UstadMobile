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
import com.ustadmobile.lib.db.entities.respect.RespectLesson.Companion.TABLE_ID

@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(
    arrayOf(
        Trigger(
            name = "respectlesson_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [TRIGGER_UPSERT],
        )
    )
)
@Entity
data class RespectLesson(
    @PrimaryKey
    var rlUid: Long = 0,
    var rlRaUid: Long = 0,
    @ReplicateEtag
    @ReplicateLastModified
    var rlLastModified: Long = 0,
    var rlUri: String = "",
    var rlExpectedDuration: Long = 0,
    var rlTitle: String = "",
    var rlDescription: String = "",
) {

    companion object {
        const val TABLE_ID = 4202
    }

}
