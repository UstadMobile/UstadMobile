package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity(indices = [
    Index(value = ["usPersonUid", "usStatus", "usClientNodeId"], name = "person_status_node_idx"),
    Index(value = ["usClientNodeId", "usStatus", "usPersonUid"], name = "node_status_person_idx")])
@Serializable
@ReplicateEntity(
    tableId = UserSession.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
 Trigger(
     name = "usersession_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [TRIGGER_UPSERT],
 )
))
class UserSession {

    @PrimaryKey(autoGenerate = true)
    var usUid: Long = 0

    @MasterChangeSeqNum
    var usPcsn: Long = 0

    @LocalChangeSeqNum
    var usLcsn: Long = 0

    @LastChangedBy
    var usLcb: Int = 0

    @ReplicateEtag
    @ReplicateLastModified
    var usLct: Long = 0

    var usPersonUid: Long = 0

    var usClientNodeId: Long = 0

    var usStartTime: Long = 0

    var usEndTime: Long = Long.MAX_VALUE

    var usStatus: Int = 0

    var usReason: Int = 0

    var usAuth: String? = null

    var usSessionType: Int = TYPE_STANDARD

    companion object {

        const val TABLE_ID = 679

        const val TYPE_STANDARD = 1

        //Session that will not be synced, it is only added to allow an upstream node to have access
        // so that findUnsentEntities will work as expected
        @Suppress("unused") //reserved for future use
        const val TYPE_UPSTREAM = 2

        const val TYPE_GUEST = 4

        /**
         * This is a temporary local session that was auto created by the account manager. It will
         * not be sent to the upstream server and will not be displayed to the user.
         */
        const val TYPE_TEMP_LOCAL = 8

        const val STATUS_ACTIVE = 1

        @Suppress("unused") //reserved for future use
        const val STATUS_NEEDS_REAUTH = 2

        const val STATUS_LOGGED_OUT = 4

        const val REASON_LOGGED_OUT = 1

        const val REASON_CONSENT_REVOKED = 2

        @Suppress("unused") //reserved for future use
        const val REASON_PASSWORD_CHANGED = 3

    }

}