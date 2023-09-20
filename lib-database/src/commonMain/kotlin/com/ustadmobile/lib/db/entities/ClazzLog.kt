package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * Represents one session (e.g. day) in the class log book. This is related to attendance records, but
 * could also be related to behavior logs etc. in the future.
 */

@ReplicateEntity(
    tableId =  ClazzLog.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
 Trigger(
     name = "clazzlog_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         TRIGGER_UPSERT_WHERE_NEWER
     ]
 )
))
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

    @ReplicateLastModified
    @ReplicateEtag
    var clazzLogLastChangedTime: Long = 0

    constructor(clazzLogUid: Long, clazzUid: Long, logDate: Long, scheduleUid: Long): this() {
        this.clazzLogUid = clazzLogUid
        this.clazzLogClazzUid = clazzUid
        this.logDate = logDate
        this.clazzLogScheduleUid = scheduleUid
    }

    companion object {

        const val TABLE_ID = 14

        const val STATUS_CREATED = 0

        const val STATUS_HOLIDAY = 1

        const val STATUS_MANUALLYCANCELED = 2

        const val STATUS_RECORDED = 4

        const val STATUS_RESCHEDULED = 8

        const val STATUS_INACTIVE = 16
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzLog

        if (clazzLogUid != other.clazzLogUid) return false
        if (clazzLogClazzUid != other.clazzLogClazzUid) return false
        if (logDate != other.logDate) return false
        if (timeRecorded != other.timeRecorded) return false
        if (clazzLogDone != other.clazzLogDone) return false
        if (cancellationNote != other.cancellationNote) return false
        if (clazzLogCancelled != other.clazzLogCancelled) return false
        if (clazzLogNumPresent != other.clazzLogNumPresent) return false
        if (clazzLogNumAbsent != other.clazzLogNumAbsent) return false
        if (clazzLogNumPartial != other.clazzLogNumPartial) return false
        if (clazzLogScheduleUid != other.clazzLogScheduleUid) return false
        if (clazzLogStatusFlag != other.clazzLogStatusFlag) return false
        if (clazzLogMSQN != other.clazzLogMSQN) return false
        if (clazzLogLCSN != other.clazzLogLCSN) return false
        if (clazzLogLCB != other.clazzLogLCB) return false
        if (clazzLogLastChangedTime != other.clazzLogLastChangedTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazzLogUid.hashCode()
        result = 31 * result + clazzLogClazzUid.hashCode()
        result = 31 * result + logDate.hashCode()
        result = 31 * result + timeRecorded.hashCode()
        result = 31 * result + clazzLogDone.hashCode()
        result = 31 * result + (cancellationNote?.hashCode() ?: 0)
        result = 31 * result + clazzLogCancelled.hashCode()
        result = 31 * result + clazzLogNumPresent
        result = 31 * result + clazzLogNumAbsent
        result = 31 * result + clazzLogNumPartial
        result = 31 * result + clazzLogScheduleUid.hashCode()
        result = 31 * result + clazzLogStatusFlag
        result = 31 * result + clazzLogMSQN.hashCode()
        result = 31 * result + clazzLogLCSN.hashCode()
        result = 31 * result + clazzLogLCB
        result = 31 * result + clazzLogLastChangedTime.hashCode()
        return result
    }


}
