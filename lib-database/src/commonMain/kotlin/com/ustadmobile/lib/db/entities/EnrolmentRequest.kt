package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers

@Entity(
    indices = arrayOf(
        Index("erClazzUid", "erStatus", name = "idx_enrolmentrequest_by_clazz"),
        Index("erPersonUid", "erStatus", name = "idx_enrolmentrequest_by_person"),
    )
)
@ReplicateEntity(
    tableId = EnrolmentRequest.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
    Trigger(
        name = "enrolmentrequest_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT]
    )
))
data class EnrolmentRequest(
    @PrimaryKey(autoGenerate = true)
    var erUid: Long = 0,
    var erClazzUid: Long = 0,
    var erClazzName: String? = null,
    var erPersonUid: Long = 0,
    var erPersonFullname:  String? = null,
    var erPersonPictureUri: String? = null,
    var erPersonUsername: String? = null,
    var erRole: Int = 0,
    var erRequestTime: Long = 0,
    var erStatus: Int = STATUS_PENDING,
    var erStatusSetByPersonUid: Long = 0,
    var erDeleted: Boolean = false,
    var erStatusSetAuth: String? = null,

    @ReplicateEtag
    @ReplicateLastModified
    var erLastModified: Long = 0,
) {

    companion object {

        const val STATUS_PENDING = 1

        const val STATUS_APPROVED = 2

        const val STATUS_REJECTED = 3

        const val STATUS_CANCELED = 4

        const val TABLE_ID = 10070

    }

}