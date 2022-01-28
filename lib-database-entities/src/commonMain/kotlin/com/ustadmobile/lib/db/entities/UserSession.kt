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
@ReplicateEntity(tableId = UserSession.TABLE_ID, tracker = UserSessionReplicate::class,
    priority = ReplicateEntity.HIGHEST_PRIORITY)
@Triggers(arrayOf(
 Trigger(
     name = "usersession_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO UserSession(usUid, usPcsn, usLcsn, usLcb, usLct, usPersonUid, usClientNodeId, usStartTime, usEndTime, usStatus, usReason, usAuth, usSessionType) 
         VALUES (NEW.usUid, NEW.usPcsn, NEW.usLcsn, NEW.usLcb, NEW.usLct, NEW.usPersonUid, NEW.usClientNodeId, NEW.usStartTime, NEW.usEndTime, NEW.usStatus, NEW.usReason, NEW.usAuth, NEW.usSessionType) 
         /*psql ON CONFLICT (usUid) DO UPDATE 
         SET usPcsn = EXCLUDED.usPcsn, usLcsn = EXCLUDED.usLcsn, usLcb = EXCLUDED.usLcb, usLct = EXCLUDED.usLct, usPersonUid = EXCLUDED.usPersonUid, usClientNodeId = EXCLUDED.usClientNodeId, usStartTime = EXCLUDED.usStartTime, usEndTime = EXCLUDED.usEndTime, usStatus = EXCLUDED.usStatus, usReason = EXCLUDED.usReason, usAuth = EXCLUDED.usAuth, usSessionType = EXCLUDED.usSessionType
         */"""
     ]
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

    @ReplicationVersionId
    @LastChangedTime
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
        const val TYPE_UPSTREAM = 2

        const val STATUS_ACTIVE = 1

        const val STATUS_NEEDS_REAUTH = 2

        const val STATUS_LOGGED_OUT = 4

        const val REASON_LOGGED_OUT = 1

        const val REASON_CONSENT_REVOKED = 2

        const val REASON_PASSWORD_CHANGED = 3

        const val USER_SESSION_NOT_LOCAL_DEVICE_SQL = """
            UserSession.usClientNodeId != (
                 SELECT nodeClientId 
                   FROM SyncNode
                  LIMIT 1)
        """

    }

}