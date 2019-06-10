package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
@Entity
class ClazzLogAttendanceRecord() {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var clazzLogAttendanceRecordUid: Long = 0

    var clazzLogClazzLogUid: Long = 0

    var studentClazzMemberUid: Long = 0

    var attendanceStatus: Int = 0

    companion object {

        const val STATUS_ATTENDED = 1

        const val STATUS_ABSENT = 2

        const val STATUS_PARTIAL = 4
    }
}
