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
        sqlStatements = [
            TRIGGER_UPSERT_WHERE_NEWER
        ]
    )
))
@Serializable
class PeerReviewerAllocation {

    @PrimaryKey(autoGenerate = true)
    var praUid: Long = 0

    // peer that is marking the assignment
    var praMarkerSubmitterUid: Long = 0

    // peer that is being marked
    var praToMarkerSubmitterUid: Long = 0

    var praAssignmentUid: Long = 0

    var praActive: Boolean = true

    @ReplicateLastModified
    @ReplicateEtag
    var praLct: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PeerReviewerAllocation) return false

        if (praUid != other.praUid) return false
        if (praMarkerSubmitterUid != other.praMarkerSubmitterUid) return false
        if (praToMarkerSubmitterUid != other.praToMarkerSubmitterUid) return false
        if (praAssignmentUid != other.praAssignmentUid) return false
        if (praActive != other.praActive) return false
        if (praLct != other.praLct) return false

        return true
    }

    override fun hashCode(): Int {
        var result = praUid.hashCode()
        result = 31 * result + praMarkerSubmitterUid.hashCode()
        result = 31 * result + praToMarkerSubmitterUid.hashCode()
        result = 31 * result + praAssignmentUid.hashCode()
        result = 31 * result + praActive.hashCode()
        result = 31 * result + praLct.hashCode()
        return result
    }

    companion object {

        const val TABLE_ID = 140

    }




}