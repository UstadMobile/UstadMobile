package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = Report.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(
    arrayOf(
        Trigger(
            name = "report_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [TRIGGER_UPSERT],
        )
    )
)
data class Report(
    @PrimaryKey(autoGenerate = true)
    @ReplicateEtag
    var reportUid: Long = 0,

    var reportTitle: String? = null,

    var reportOptions: String? = null,

    var reportIsTemplate: Boolean = false,

    @ReplicateLastModified
    var reportLastModTime: Long = 0
) {
    companion object {
        const val TABLE_ID = 101
    }
}

