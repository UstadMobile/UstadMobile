package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@ReplicateEntity(
    tableId = ClazzLogAttendanceRecord.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Entity
@Serializable
@Triggers(arrayOf(
 Trigger(
     name = "clazzlogattendancerecord_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [TRIGGER_UPSERT],
 )
))
open class ClazzLogAttendanceRecord {

    @PrimaryKey(autoGenerate = true)
    var clazzLogAttendanceRecordUid: Long = 0

    var clazzLogAttendanceRecordClazzLogUid: Long = 0

    var clazzLogAttendanceRecordPersonUid: Long = 0

    var attendanceStatus: Int = 0

    @MasterChangeSeqNum
    var clazzLogAttendanceRecordMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzLogAttendanceRecordLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzLogAttendanceRecordLastChangedBy: Int = 0

    @ReplicateLastModified
    @ReplicateEtag
    var clazzLogAttendanceRecordLastChangedTime: Long = 0


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzLogAttendanceRecord

        if (clazzLogAttendanceRecordUid != other.clazzLogAttendanceRecordUid) return false
        if (clazzLogAttendanceRecordClazzLogUid != other.clazzLogAttendanceRecordClazzLogUid) return false
        if (clazzLogAttendanceRecordPersonUid != other.clazzLogAttendanceRecordPersonUid) return false
        if (attendanceStatus != other.attendanceStatus) return false
        if (clazzLogAttendanceRecordMasterChangeSeqNum != other.clazzLogAttendanceRecordMasterChangeSeqNum) return false
        if (clazzLogAttendanceRecordLocalChangeSeqNum != other.clazzLogAttendanceRecordLocalChangeSeqNum) return false
        if (clazzLogAttendanceRecordLastChangedBy != other.clazzLogAttendanceRecordLastChangedBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazzLogAttendanceRecordUid.hashCode()
        result = 31 * result + clazzLogAttendanceRecordClazzLogUid.hashCode()
        result = 31 * result + clazzLogAttendanceRecordPersonUid.hashCode()
        result = 31 * result + attendanceStatus
        result = 31 * result + clazzLogAttendanceRecordMasterChangeSeqNum.hashCode()
        result = 31 * result + clazzLogAttendanceRecordLocalChangeSeqNum.hashCode()
        result = 31 * result + clazzLogAttendanceRecordLastChangedBy
        return result
    }


    companion object {


        const val TABLE_ID = 15

        const val STATUS_ATTENDED = 1

        const val STATUS_ABSENT = 2

        const val STATUS_PARTIAL = 4

        const val ATTENDANCE_THRESHOLD_GOOD = 0.8F

        const val ATTENDANCE_THRESHOLD_WARNING = 0.6F

    }


}
