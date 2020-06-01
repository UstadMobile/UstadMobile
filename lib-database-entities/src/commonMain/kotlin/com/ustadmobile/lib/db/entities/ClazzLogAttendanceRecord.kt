package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 15)
@Entity
@Serializable
open class ClazzLogAttendanceRecord() {

    @PrimaryKey(autoGenerate = true)
    var clazzLogAttendanceRecordUid: Long = 0

    var clazzLogAttendanceRecordClazzLogUid: Long = 0

    var clazzLogAttendanceRecordClazzMemberUid: Long = 0

    var attendanceStatus: Int = 0

    @MasterChangeSeqNum
    var clazzLogAttendanceRecordMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzLogAttendanceRecordLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzLogAttendanceRecordLastChangedBy: Int = 0

    companion object {

        const val STATUS_ATTENDED = 1

        const val STATUS_ABSENT = 2

        const val STATUS_PARTIAL = 4
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzLogAttendanceRecord

        if (clazzLogAttendanceRecordUid != other.clazzLogAttendanceRecordUid) return false
        if (clazzLogAttendanceRecordClazzLogUid != other.clazzLogAttendanceRecordClazzLogUid) return false
        if (clazzLogAttendanceRecordClazzMemberUid != other.clazzLogAttendanceRecordClazzMemberUid) return false
        if (attendanceStatus != other.attendanceStatus) return false
        if (clazzLogAttendanceRecordMasterChangeSeqNum != other.clazzLogAttendanceRecordMasterChangeSeqNum) return false
        if (clazzLogAttendanceRecordLocalChangeSeqNum != other.clazzLogAttendanceRecordLocalChangeSeqNum) return false
        if (clazzLogAttendanceRecordLastChangedBy != other.clazzLogAttendanceRecordLastChangedBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazzLogAttendanceRecordUid.hashCode()
        result = 31 * result + clazzLogAttendanceRecordClazzLogUid.hashCode()
        result = 31 * result + clazzLogAttendanceRecordClazzMemberUid.hashCode()
        result = 31 * result + attendanceStatus
        result = 31 * result + clazzLogAttendanceRecordMasterChangeSeqNum.hashCode()
        result = 31 * result + clazzLogAttendanceRecordLocalChangeSeqNum.hashCode()
        result = 31 * result + clazzLogAttendanceRecordLastChangedBy
        return result
    }


}
