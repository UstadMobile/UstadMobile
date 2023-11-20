package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = CourseGroupSet.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
    Trigger(
        name = "coursegroupset_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT],
    )
))
@Serializable
class CourseGroupSet {

    @PrimaryKey(autoGenerate = true)
    var cgsUid: Long = 0

    var cgsName: String? = null

    var cgsTotalGroups: Int = 4

    var cgsActive: Boolean = true

    @ColumnInfo(index = true)
    var cgsClazzUid: Long = 0

    @ReplicateLastModified
    @ReplicateEtag
    var cgsLct: Long = 0

    companion object {

        const val TABLE_ID = 242


    }


}