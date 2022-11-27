package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzLogAttendanceRecordWithPerson : ClazzLogAttendanceRecord() {

    @Embedded
    var person: Person? = null

    fun copy() = ClazzLogAttendanceRecordWithPerson().also {
        it.person = person
        it.clazzLogAttendanceRecordUid = clazzLogAttendanceRecordUid
        it.clazzLogAttendanceRecordPersonUid = clazzLogAttendanceRecordPersonUid
        it.clazzLogAttendanceRecordClazzLogUid = clazzLogAttendanceRecordClazzLogUid
        it.clazzLogAttendanceRecordLastChangedBy = clazzLogAttendanceRecordLastChangedBy
        it.clazzLogAttendanceRecordLocalChangeSeqNum = clazzLogAttendanceRecordLocalChangeSeqNum
        it.clazzLogAttendanceRecordMasterChangeSeqNum = clazzLogAttendanceRecordMasterChangeSeqNum
        it.attendanceStatus = attendanceStatus
    }

}
