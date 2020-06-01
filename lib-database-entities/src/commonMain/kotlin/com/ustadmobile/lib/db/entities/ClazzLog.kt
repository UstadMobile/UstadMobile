package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

/**
 * Represents one session (e.g. day) in the class log book. This is related to attendance records, but
 * could also be related to behavior logs etc. in the future.
 */

@SyncableEntity(tableId = 14)
@Entity
@Serializable
open class ClazzLog()  {

    @PrimaryKey(autoGenerate = true)
    var clazzLogUid: Long = 0

    var clazzLogClazzUid: Long = 0

    var logDate: Long = 0

    var timeRecorded: Long = 0

    var clazzLogDone: Boolean = false

    var cancellationNote: String? = null

    var clazzLogCancelled: Boolean = false

    var clazzLogNumPresent: Int = 0

    var clazzLogNumAbsent: Int = 0

    var clazzLogNumPartial: Int = 0

    var clazzLogScheduleUid: Long = 0

    var clazzLogStatusFlag: Int = 0

    @MasterChangeSeqNum
    var clazzLogMSQN: Long = 0

    @LocalChangeSeqNum
    var clazzLogLCSN: Long = 0

    @LastChangedBy
    var clazzLogLCB: Int = 0

    constructor(clazzLogUid: Long, clazzUid: Long, logDate: Long, scheduleUid: Long): this() {
        this.clazzLogUid = clazzLogUid
        this.clazzLogClazzUid = clazzUid
        this.logDate = logDate
        this.clazzLogScheduleUid = scheduleUid
    }

    companion object {

        const val STATUS_CREATED = 0

        const val STATUS_HOLIDAY = 1

        const val STATUS_MANUALLYCANCELED = 2

        const val STATUS_RECORDED = 4

        const val STATUS_RESCHEDULED = 8
    }


}
