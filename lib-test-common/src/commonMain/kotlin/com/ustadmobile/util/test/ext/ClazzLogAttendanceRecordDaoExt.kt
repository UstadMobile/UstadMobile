package com.ustadmobile.util.test.ext

import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzMember

suspend fun ClazzLogAttendanceRecordDao.insertTestRecordsForClazzLog(clazzLog: ClazzLog,
    memberList: List<ClazzMember>): List<ClazzLogAttendanceRecord> {
    return memberList.map {clazzMember ->
        ClazzLogAttendanceRecord().apply {
            clazzLogAttendanceRecordClazzLogUid = clazzLog.clazzLogUid
            clazzLogAttendanceRecordClazzMemberUid = clazzMember.clazzMemberUid
            clazzLogAttendanceRecordUid = insertAsync(this)
        }
    }
}