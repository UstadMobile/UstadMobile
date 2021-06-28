package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.ClientSyncManager.Companion.TABLEID_SYNC_ALL_TABLES
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = UserSession.TABLE_ID,
    syncFindAllQuery = """
        SELECT UserSession.*
          FROM UserSession
         WHERE usClientNodeId =  :clientId
    """,
    notifyOnUpdate = ["""
        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
               $TABLEID_SYNC_ALL_TABLES AS tableId
          FROM ChangeLog
               JOIN UserSession 
                    ON ChangeLog.chTableId = ${UserSession.TABLE_ID}
                       AND ChangeLog.chEntityPk = UserSession.usUid
                        
    """])
@Serializable
class UserSession {

    @PrimaryKey(autoGenerate = true)
    var usUid: Long = 0

    @MasterChangeSeqNum
    var usPcsn: Long = 0

    @LocalChangeSeqNum
    var usLcsn: Long = 0

    @LastChangedBy
    var usLcb: Int = 0

    @LastChangedTime
    var usLct: Long = 0

    var usPersonUid: Long = 0

    var usClientNodeId: Int = 0

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

    }
}