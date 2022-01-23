package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*

@Entity
@ReplicateEntity(tableId = 419, tracker = ErrorReportReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "errorreport_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO ErrorReport(errUid, errPcsn, errLcsn, errLcb, errLct, severity, timestamp, presenterUri, appVersion, versionCode, errorCode, operatingSys, osVersion, stackTrace, message) 
         VALUES (NEW.errUid, NEW.errPcsn, NEW.errLcsn, NEW.errLcb, NEW.errLct, NEW.severity, NEW.timestamp, NEW.presenterUri, NEW.appVersion, NEW.versionCode, NEW.errorCode, NEW.operatingSys, NEW.osVersion, NEW.stackTrace, NEW.message) 
         /*psql ON CONFLICT (errUid) DO UPDATE 
         SET errPcsn = EXCLUDED.errPcsn, errLcsn = EXCLUDED.errLcsn, errLcb = EXCLUDED.errLcb, errLct = EXCLUDED.errLct, severity = EXCLUDED.severity, timestamp = EXCLUDED.timestamp, presenterUri = EXCLUDED.presenterUri, appVersion = EXCLUDED.appVersion, versionCode = EXCLUDED.versionCode, errorCode = EXCLUDED.errorCode, operatingSys = EXCLUDED.operatingSys, osVersion = EXCLUDED.osVersion, stackTrace = EXCLUDED.stackTrace, message = EXCLUDED.message
         */"""
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