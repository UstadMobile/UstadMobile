package com.ustadmobile.util.test.ext

import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzEnrollment

suspend fun ClazzLogAttendanceRecordDao.insertTestRecordsForClazzLog(clazzLog: ClazzLog,
                                                                     enrollmentList: List<ClazzEnrollment>): List<ClazzLogAttendanceRecord> {
    return enrollmentList.map { clazzMember ->
        ClazzLogAttendanceRecord().apply {
            clazzLogAttendanceRecordClazzLogUid = clazzLog.clazzLogUid
            clazzLogAttendanceRecordPersonUid = clazzMember.clazzEnrollmentPersonUid
            clazzLogAttendanceRecordUid = insertAsync(this)
        }
    }
}