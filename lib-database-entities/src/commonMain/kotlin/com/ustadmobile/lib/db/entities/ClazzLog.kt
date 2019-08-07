package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

/**
 * Represents one session (e.g. day) in the class log book. This is related to attendance records, but
 * could also be related to behavior logs etc. in the future.
 */
@UmEntity(tableId = 14)
@Entity
open class ClazzLog() {

    @SyncablePrimaryKey
    @PrimaryKey(autoGenerate = true)
    var clazzLogUid: Long = 0

    var clazzLogClazzUid: Long = 0

    var logDate: Long = 0

    var timeRecorded: Long = 0

    var clazzLogDone: Boolean = false

    var clazzLogCancelled: Boolean = false

    var clazzLogNumPresent: Int = 0

    var clazzLogNumAbsent: Int = 0

    var clazzLogNumPartial: Int = 0

    var clazzLogScheduleUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var clazzLogMSQN: Long = 0

    @UmSyncLocalChangeSeqNum
    var clazzLogLCSN: Long = 0

    @UmSyncLastChangedBy
    var clazzLogLCB: Int = 0

    constructor(clazzLogUid: Long, clazzUid: Long, logDate: Long, scheduleUid: Long): this() {
        this.clazzLogUid = clazzLogUid
        this.clazzLogClazzUid = clazzUid
        this.logDate = logDate
        this.clazzLogScheduleUid = scheduleUid
    }


}
