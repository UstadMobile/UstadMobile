package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*

@Entity
@ReplicateEntity(tableId = 419, tracker = ErrorReportTracker::class)
//@SyncableEntity(tableId = 419)
class ErrorReport {

    @PrimaryKey(autoGenerate = true)
    var errUid: Long = 0

    @MasterChangeSeqNum
    var errPcsn: Long = 0

    @LocalChangeSeqNum
    var errLcsn: Long = 0

    @LastChangedBy
    var errLcb: Int = 0

    @LastChangedTime
    @ReplicationVersionId
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