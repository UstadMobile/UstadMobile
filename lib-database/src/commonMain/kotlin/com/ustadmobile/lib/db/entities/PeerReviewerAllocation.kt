package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = PeerReviewerAllocation.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
    Trigger(
        name = "peerreviewerallocation_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT],
    )
))
@Serializable
data class PeerReviewerAllocation(
    @PrimaryKey(autoGenerate = true)
    var praUid: Long = 0,

    // peer that is marking the assignment
    var praMarkerSubmitterUid: Long = 0,

    // peer that is being marked
    var praToMarkerSubmitterUid: Long = 0,

    var praAssignmentUid: Long = 0,

    var praActive: Boolean = true,

    @ReplicateLastModified
    @ReplicateEtag
    var praLct: Long = 0,
) {

    companion object {

        const val TABLE_ID = 140

    }


}