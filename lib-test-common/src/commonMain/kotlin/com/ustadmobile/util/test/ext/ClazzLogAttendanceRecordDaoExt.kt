package com.ustadmobile.util.test.ext

import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzEnrolment

suspend fun ClazzLogAttendanceRecordDao.insertTestRecordsForClazzLog(clazzLog: ClazzLog,
                                                                     enrolmentList: List<ClazzEnrolment>): List<ClazzLogAttendanceRecord> {
    return enrolmentList.map { clazzMember ->
        ClazzLogAttendanceRecord().apply {
            clazzLogAttendanceRecordClazzLogUid = clazzLog.clazzLogUid
            clazzLogAttendanceRecordPersonUid = clazzMember.clazzEnrolmentPersonUid
            clazzLogAttendanceRecordUid = insertAsync(this)
        }
    }
}