package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*

@Entity
@ReplicateEntity(
    tableId = 419,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
 Trigger(
     name = "errorreport_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         TRIGGER_UPSERT_WHERE_NEWER
     ]
 )
))
class ErrorReport {

    @PrimaryKey(autoGenerate = true)
    var errUid: Long = 0

    @MasterChangeSeqNum
    var errPcsn: Long = 0

    @LocalChangeSeqNum
    var errLcsn: Long = 0

    @LastChangedBy
    var errLcb: Int = 0

    @ReplicateLastModified
    @ReplicateEtag
    var errLct: Long = 0

    var severity: Int = 0

    var timestamp: Long = 0

    var presenterUri: String? = null

    var appVersion: String? = null

    var versionCode: Int = 0

    var errorCode: Int = 0

    var operatingSys: String? = null

    var osVersion: String? = null

    var stackTrace: String? = null

    var message: String? = null

    companion object {


        //Warning
        const val SEVERITY_WARNING = 1

        //Error
        const val SEVERITY_ERROR = 2

        //What a terrible failure
        const val SEVERITY_WTF = 3

    }
}